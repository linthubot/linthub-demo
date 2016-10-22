/*
	 _______ __
	|   _   |__.-----.
	|.  1   |  |     |       BIN BUILD SYSTEM
	|.  _   |__|__|__|   github.com/bin-build
	|:  1    \ copyright (c) 2016, josh junon
	|::.. .  / licensed under the MIT license
	`-------'

	The runner performs all of the traversals
	and runs all of the tasks.
*/

#include <atomic>
#include <condition_variable>
#include <errno.h>
#include <mutex>
#include <signal.h>
#include <string.h>
#include <thread>
#include <vector>

#include "exception.h"
#include "report.h"
#include "runner.h"

using namespace bin;
using namespace std;

// TODO trap sigint
static volatile bool interrupted = false;
static void handleInterrupt(int sig) {
	(void) sig;
	interrupted = true;
}

static void execute_single(Traverser &traverser, bool stopOnFirst) {
	while (!traverser.exhausted()) {
		Task task;
		TID taskId = traverser.next(task);
		if (taskId == 0 || !task) {
			return;
		}

		Report::runBegin(0, taskId);
		int code = task();
		Report::runEnd(0, taskId, code);

		if (code == 0) {
			traverser.report(taskId);
		} else if (stopOnFirst) {
			return;
		}
	}
}

static void execute_multiple(Traverser &traverser, int count, bool stopOnFirst) {
	vector<thread> threads(count);
	condition_variable cv;
	mutex cvmtx;
	atomic_bool stop(false);
	atomic_uint running(count);

	for (int i = 0; i < count; i++) {
		threads.at(i) = thread([i, stopOnFirst, &cv, &cvmtx, &stop, &traverser, &running]() {
			while (!(stop || interrupted) && !traverser.exhausted()) {
				unique_lock<mutex> ulock(cvmtx);

				TID taskId;
				Task task;
				while (!(stop || interrupted) && !traverser.exhausted() && (taskId = traverser.next(task)) == 0) {
					if (running.fetch_sub(1) == 1) {
						// we've stalled; drink all the koolaid.
						stop = true;
						cv.notify_all();
						return;
					}

					cv.wait(ulock);

					if (stop) {
						return;
					}

					++running;
				}

				Report::runBegin(i, taskId);
				int code = task();
				Report::runEnd(i, taskId, code);

				if (code == 0) {
					traverser.report(taskId);
				} else {
					stop = stopOnFirst;
				}

				cv.notify_all();
			}
		});
	}

	for (thread &t : threads) {
		t.join();
	}
}

void bin::execute(Traverser &traverser, int count, bool stopOnFirst) {
	sig_t oldsignal = signal(SIGINT, &handleInterrupt);
	if (oldsignal == SIG_ERR) {
		throw error << "could not establish SIGINT trap: " << strerror(errno);
	}

	int coreCount = count;
	if (count < 1) {
		thread::hardware_concurrency();
	}

	Report::processorConfig(coreCount);

	if (traverser.exhausted()) {
		Report::buildEmpty();
		return;
	}

	Report::buildStart();
	if (coreCount < 2) {
		execute_single(traverser, stopOnFirst);
	} else {
		execute_multiple(traverser, coreCount, stopOnFirst);
	}
	Report::buildEnd(traverser.exhausted());
}

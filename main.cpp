#include "stdio.h"
#include "string.h"
#include "stdlib.h"
#define MAXLOOP 10

typedef enum {a, c=4, d} Abc;
int fact(int, int*);
void dead();
Abc u;

int i;

int main(int argc, char** argv) {

    int* leakyBuffer = malloc(sizeof(int)*10);

    // commented line of code
    // and a condition not handled
	// if (argc != 2) return 1;

    int saturated = 0x100000000;

    int r = fact(atoi(argv[1]), leakyBuffer);

	printf("Input Factorial(%i): %i\n", atoi(argv[1]), r);

    char b[5] = {0};
	for (i = 0; i < MAXLOOP; i++) {
		b[i] = 1;
	}

    return 0;

}

int fact(int x, int* p) {

    p++;

    if (x == 1 || x == 0) {
        return 1;
    } else {
        return x * fact(x-1, p);
    }

}

int y;
void dead() {

    printf("dead code");
    y = 0;
    y++;

}



# -*- coding: utf-8 -*- 
#!/usr/bin/python 
#author:Vincent


from time import sleep
import thread
import urllib2
# Event Manager

event_listeners = {}

def fire_event(name):
    event_listeners[name]()

def use_event(func):
    def call(*args, **kwargs):
        generator = func(*args, **kwargs)
        # 执行到挂起
        event_name = next(generator)
        # 将“唤醒挂起的协程”注册到事件管理器中
        def resume():
            try:
                next(generator)
            except StopIteration:
                pass
        # event_listeners[event_name] = resume
        event_listeners['e2'] = resume
        event_listeners['e1'] = resume

    return call


def test_fun():
	print "begin"
	sleep(5)
	print "end"
# Test

@use_event
def test_work():
    for x in xrange(1,10):
        yield "e1"  # 挂起当前协程, 等待事件
        print("=" * 50)
        # print("waiting click")
        print "add_to_queue"
        yield "e2"
        print "shangbao"

        # yield test_fun()
        # print("clicked !!")
        sleep(2)


def gen_fun():
    for x in xrange(1,10):
        yield 'e1'
        print "add_to_queue"
        yield 'e2'
        print "shangbao"

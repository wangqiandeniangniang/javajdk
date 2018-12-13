package com.example.javajdk.srcjdk.java.util;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @ClassName ChorusLine
 * @Description 队列大合唱
 * @Author chen.liang
 * @Date 2018/12/4 18:56
 * @Version 1.0
 **/
public class ChorusLine {

    private interface  Tweaker {
        void run(Deque<Integer> deque);
    }


    private  final static Tweaker[] tweakers = {
        new Tweaker() {
            @Override
            public void run(Deque<Integer> deque) {
                for (int i = 0; i < 7; i++) {
                    deque.addLast(i);
                }
                deque.removeFirst();
                deque.removeFirst();
                deque.addLast(7);
                deque.addLast(8);
                Iterator<Integer> it = deque.descendingIterator();
                equal(it.next(), 8);
                it.remove();
                try {
                    it.remove();
                } catch (IllegalStateException e) {
                    pass();
                } catch (Throwable throwable) {
                    unexpected(throwable);
                }

                deque.addLast(9);
                it = deque.descendingIterator();
                equal(it.next(), 9);
                equal(it.next(), 7);
                it.remove();
                try {
                    it.remove();
                } catch (IllegalStateException e) {
                    pass();
                } catch (Throwable throwable) {
                    unexpected(throwable);
                }
                equal(it.next(), 6);
                System.out.println(deque);
            }
        },
            new Tweaker() {
                @Override
                public void run(Deque<Integer> deque) {
                    deque.clear();
                    check(deque.isEmpty());
                    check(deque.size() == 0);
                    check(!deque.iterator().hasNext());
                    check(!deque.descendingIterator().hasNext());
                    try {
                        deque.descendingIterator().next();
                        fail();
                    } catch (NoSuchElementException e) {
                        pass();
                    } catch (Throwable throwable) {
                        unexpected(throwable);
                    }
                }
            },
            new Tweaker() {
                @Override
                public void run(Deque<Integer> deque) {
                    for (int i = 0; i < 11; i++) {
                        deque.add(i);
                    }
                    Iterator<Integer> iterator = deque.iterator();
                    equal(iterator.next(), 0);
                    equal(iterator.next(), 1);
                    iterator.remove();
                    deque.addFirst(-1);
                    deque.addFirst(-2);
                    iterator = deque.iterator();
                    equal(iterator.next(), -2);
                    equal(iterator.next(), -1);
                    equal(iterator.next(), 0);
                    iterator.remove();

                    iterator = deque.descendingIterator();
                    try {
                        iterator.remove();
                        fail();
                    } catch (IllegalStateException e) {
                        pass();
                    } catch (Throwable throwable) {
                        unexpected(throwable);
                    }
                    equal(iterator.next(), 9);
                    equal(iterator.next(), 8);
                    iterator.remove();
                    System.out.println(deque);
                }
            },
            new Tweaker() {
                @Override
                public void run(Deque<Integer> deque) {
                    while (deque.size() > 1) {
                        Iterator<Integer> it = deque.iterator();
                        it.next();
                        it.remove();
                        it = deque.descendingIterator();
                        it.next();
                        it.remove();
                    }
                    System.out.println(deque);

                }
            }

    };

    public static void main(String[] args) {
        try {
            realMain(args);
        } catch (Throwable throwable) {
            unexpected(throwable);
        }
        System.out.printf("%nPassed = %d, failed= %d%n%n", passed, failed);
        if (failed > 0) {
            throw new AssertionError("Some tests failed");
        }
    }

    private static void realMain(String[] args) {
        Collection<Deque<Integer>> deques = new ArrayDeque<>();
        deques.add(new ArrayDeque<>());
        deques.add(new LinkedList<>());
        deques.add(new LinkedBlockingDeque<>());
        deques.add(new ConcurrentLinkedDeque<>());

        equal(deques);
        for (Tweaker tweaker : tweakers) {
            for (Deque<Integer> deque : deques) {
                tweaker.run(deque);
                equal(deques);
            }
        }
    }

    private static void equal(Iterable<Deque<Integer>> deques) {
        Deque<Integer> prev = null;
        for (Deque<Integer> deque : deques) {
            if (prev != null) {
                equal(prev.isEmpty(), deque.isEmpty());
                equal(prev.size(), deque.size());
                equal(prev.toString(), deque.toString());
            }
            prev = deque;
        }

        ArrayDeque<Iterator<Integer>> its = new ArrayDeque<>();
        for (Deque<Integer> deque : deques) {
            its.addLast(deque.iterator());
        }
        equal(its);
    }

    private static void equal(Deque<Iterator<Integer>> its) {
        Iterator<Integer> remove = its.remove();
        while (remove.hasNext()) {
            Integer i = remove.next();
            for (Iterator<Integer> it : its) {
                equal(it.next(), i);
            }
        }
        for (Iterator<Integer> it : its) {
            check(!it.hasNext());
            try {
                it.next();
                fail();
            } catch (NoSuchElementException e) {
                pass();
            } catch (Throwable throwable) {
                unexpected(throwable);
            }
        }
    }

    //----------------------Infrastructure-----------------------
    static  volatile  int passed = 0, failed = 0;
    static void pass() {
        passed++;
    }
    static  void fail() {
        failed++;
        Thread.dumpStack();
    }

    static void fail(String msg) {
        System.out.println(msg);
        fail();
    }

    static void unexpected(Throwable throwable) {
        failed++;
        throwable.printStackTrace();
    }

    static void check(boolean cond) {
        if (cond) {
            pass();
        } else {
            fail();
        }
    }

    static void equal(Object x, Object y) {
        if(x == null ? y == null: x.equals(y)) pass();
        else fail(x + " not equal to " + y);
    }

}

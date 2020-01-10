package com.springboot.bean;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class UnsafeTest {

    public class Car {

        private String name;

        public Car() {
        }

        public Car(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Test
    public void testUnsafe() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        Car car = new Car("bmw");

        //Unsafe unsafe = new Unsafe(); // private Unsafe() { }
        //获取unsafe，私有的构造方法，没法直接new，只能用反射获取
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);

        //1.操作对象和属性  objectFieldOffset   可用于你想跳过构造器的场景或者对象初始化的场景，绕过安全检查等 allocateInstance
        Field carName = Car.class.getDeclaredField("name");
        //获取对象属性的偏移地址
        long carNameOffsite = unsafe.objectFieldOffset(carName);
        //unsafe类操作类的属性，即便是私有的
        unsafe.putObject(car, carNameOffsite, "mini");
        System.out.println(car.getName());

        //2.操作数组  arrayBaseOffset  arrayIndexScale 可用于一些大数组的场景，Integer.MAX_VALUE，堆外内存技术
        int[] a = {1, 2, 3, 4, 5};
        //获取数组的第一个元素的偏移地址
        int aFirstOffsite = unsafe.arrayBaseOffset(a.getClass());
        //获取数组中元素的增量地址
        int aEachOffsite = unsafe.arrayIndexScale(a.getClass());
        //需要修改元素的位置  把3改成998
        long three = aFirstOffsite + 2 * aEachOffsite;
        unsafe.putInt(a, three, 998);
        System.out.println(a[2]);


        //3.线程的挂起和恢复、cas  LockSupport类的park()主要就是用了Unsafe.park()
       Thread t = new Thread(() -> {
            long start = System.currentTimeMillis();
            //纳秒，相对时间
            unsafe.park(false, 5000000000L);
            System.out.println(System.currentTimeMillis() - start + "ms");
        });
        t.start();
        TimeUnit.SECONDS.sleep(3);
        //阻塞5秒，3s提前唤醒，结束线程
        unsafe.unpark(t);

     Thread t1 = new Thread(() -> {
            long start = System.currentTimeMillis();
            //纳秒，相对时间
            unsafe.park(false, 5000000000L);
            System.out.println(System.currentTimeMillis() - start + "ms");
        });
        t1.start();
        TimeUnit.SECONDS.sleep(10);
        //阻塞5秒，睡10s，输出结果，等10s完成才结束线程
        unsafe.unpark(t1);



        //现在3的位置存的是998  把a的偏移量three 的 998 换成 100
        //如果我提前修改了里面的值，改称为250，那么这个b值应该是：false
        //a[2] = 250;
        boolean b = unsafe.compareAndSwapInt(a, three, 998, 100);
        System.out.println(b);
        System.out.println(a[2]);
    }
}

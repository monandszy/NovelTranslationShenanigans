package code.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SomeClass {

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    Object m1 = new Object();
    Object m2 = new Object();

    ExecutorService service = Executors.newFixedThreadPool(2);
    Future<?> f1 = service.submit(() -> {
      synchronized (m1) {
        System.out.println("s `1");
        synchronized (m2) {
          System.out.println("m");
          return " 1";
        }
      }
    });
    Future<?> f2 = service.submit(() -> {
      synchronized (m2) {
        System.out.println("s 2");
        synchronized (m1) {
          System.out.println("m2");
          int a = 3;
          a = 4;
          return " 2";
        }
      }
    });

    f2.get();
    f1.get();

  }
}
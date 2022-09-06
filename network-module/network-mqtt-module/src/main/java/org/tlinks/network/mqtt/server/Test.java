package org.tlinks.network.mqtt.server;

import lombok.Data;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author : zzh
 * create at:  2022/9/2
 * @description:
 */
public class Test {

    private static final EmitterProcessor<Demo> connectionProcessor = EmitterProcessor.create(false);

    private static final FluxSink<Demo> sink = connectionProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void sinkDemo() throws InterruptedException {
        Sinks.Many<Demo> all = Sinks.many().replay().all();

        executorService.execute(() -> {
            all.asFlux()
                    .doOnNext(demo -> {
                        System.out.println("subscribe-A:" + demo.getId());
                    })
                    .subscribe();
        });

        executorService.execute(() -> {
            all.asFlux()
                    .doOnNext(demo -> {
                        System.out.println("subscribe-B:" + demo.getId());
                    })
                    .subscribe();
        });

        Thread.sleep(5000L);

        System.out.println("subscribe count:" + all.currentSubscriberCount());

        for (int i = 0; i < 5; i++) {
            System.out.println("==> publish:" + i);
            all.tryEmitNext(new Demo(i));
            Thread.sleep(3000L);
        }

    }

    public static void processorDemo() throws InterruptedException {

        System.out.println(connectionProcessor.hasDownstreams());

        executorService.execute(() -> {
            connectionProcessor
                    .map(Function.identity())
                    .doOnNext(demo -> {
                        System.out.println("subscribe:" + demo.getId());
                    })
                    //.flatMap(demo -> System.out.println(demo.getId()))
                    .subscribe();
        });

        Thread.sleep(5000L);

        System.out.println(connectionProcessor.hasDownstreams());

        for (int i = 0; i < 5; i++) {
            System.out.println("==> publish:" + i);
            sink.next(new Demo(i));
            Thread.sleep(3000L);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        //processorDemo();

        sinkDemo();

    }

    @Data
    static class Demo {
        private Integer id;

        public Demo(Integer id) {
            this.id = id;
        }
    }
}

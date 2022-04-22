package ru.itis.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itis.grpc.proto.service.CalculateRequest;
import ru.itis.grpc.proto.service.CalculateResponse;
import ru.itis.grpc.proto.service.CalculatorGrpc;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GeneralClient {

    private static final String TARGET = "localhost:50051";

    private static final Logger logger = LoggerFactory.getLogger(GeneralClient.class);

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(TARGET)
                .usePlaintext()
                .build();
        try {
            unaryCall(channel);
            clientStreaming(channel);
            serverStreaming(channel);
            biDirectionalStreaming(channel);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static void unaryCall(ManagedChannel channel) {
        logger.info("Make unary call");

        CalculatorGrpc.CalculatorFutureStub futureStub = getFutureStub(channel);
        CalculateRequest calculateRequest = CalculateRequest.newBuilder()
                .setNum(getRandomNum(0, 1000))
                .build();
        ListenableFuture<CalculateResponse> listenableFuture = futureStub.getSqrt(calculateRequest);

        try {
            logger.debug("Unary call. Response from server: {}", listenableFuture.get().getNum());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void clientStreaming(ManagedChannel channel) {
        System.out.println("\n");
        logger.info("Make client streaming");

        CalculatorGrpc.CalculatorStub stub = getStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<CalculateRequest> streamObserver = stub.calculateStd(new StreamObserver<>() {

            @Override
            public void onNext(CalculateResponse response) {
                logger.debug("Client streaming. Response from server: {}", response.getNum());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.stream(getRandomNumsArray(-100, 500, 30))
                .forEach(num ->
                        streamObserver.onNext(CalculateRequest.newBuilder()
                                .setNum(num)
                                .build())
                );
        streamObserver.onCompleted();
        try {
            latch.await(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void serverStreaming(ManagedChannel channel) {
        System.out.println("\n");
        logger.info("Make server streaming");

        CalculatorGrpc.CalculatorBlockingStub blockingStub = getBlockingStub(channel);

        CalculateRequest calculateRequest = CalculateRequest.newBuilder()
                .setNum(getRandomNum(1, 1500))
                .build();
        blockingStub.getFactors(calculateRequest)
                .forEachRemaining(response ->
                        logger.debug("Server streaming. Response from server: {}", response.getNum())
                );
    }

    private static void biDirectionalStreaming(ManagedChannel channel) {
        System.out.println("\n");
        logger.info("Make bi-directional streaming");

        CalculatorGrpc.CalculatorStub stub = getStub(channel);

        CountDownLatch latch = new CountDownLatch(1);
        final StreamObserver<CalculateRequest> serverDataObserver = stub.getMaximum(new StreamObserver<>() {

            @Override
            public void onNext(CalculateResponse calculateResponse) {
                logger.debug("Bi-directional streaming. Response from server: {}", calculateResponse.getNum());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.stream(getRandomNumsArray(-1000, 1000, 10))
                .forEach(num -> {
                    CalculateRequest request = CalculateRequest.newBuilder()
                            .setNum(num)
                            .build();
                    serverDataObserver.onNext(request);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        serverDataObserver.onCompleted();

        try {
            latch.await(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static CalculatorGrpc.CalculatorStub getStub(Channel channel) {
        return CalculatorGrpc.newStub(channel);
    }

    private static CalculatorGrpc.CalculatorBlockingStub getBlockingStub(Channel channel) {
        return CalculatorGrpc.newBlockingStub(channel);
    }

    private static CalculatorGrpc.CalculatorFutureStub getFutureStub(Channel channel) {
        return CalculatorGrpc.newFutureStub(channel);
    }

    private static double getRandomNum(double lowerBound, double upperBound) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound must be lower than upper bound");
        }

        return Math.floor(Math.random() * (upperBound - lowerBound + 1) + lowerBound);
    }

    private static double[] getRandomNumsArray(double lowerBound, double upperBound, int size) {
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = getRandomNum(lowerBound, upperBound);
        }

        return arr;
    }
}

package ru.itis.grpc.service;

import io.grpc.stub.StreamObserver;
import ru.itis.grpc.proto.service.CalculateRequest;
import ru.itis.grpc.proto.service.CalculateResponse;
import ru.itis.grpc.proto.service.CalculatorGrpc;

import java.util.ArrayList;
import java.util.List;

public class CalculatorImpl extends CalculatorGrpc.CalculatorImplBase {

    @Override
    public void getSqrt(CalculateRequest request, StreamObserver<CalculateResponse> responseObserver) {
        double number = request.getNum();
        CalculateResponse calculateResponse = CalculateResponse.newBuilder()
                .setNum(Math.sqrt(number))
                .build();

        responseObserver.onNext(calculateResponse);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<CalculateRequest> calculateStd(StreamObserver<CalculateResponse> responseObserver) {
        return new StreamObserver<>() {
            private final List<Double> nums = new ArrayList<>();

            @Override
            public void onNext(CalculateRequest calculateRequest) {
                double num = calculateRequest.getNum();
                nums.add(num);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                double sum = 0.0;
                double standardDeviation = 0.0;
                int length = nums.size();
                for (double num : nums) {
                    sum += num;
                }
                double mean = sum / length;
                for (double num : nums) {
                    standardDeviation += Math.pow(num - mean, 2);
                }
                double std = Math.sqrt(standardDeviation / length);

                responseObserver.onNext(CalculateResponse.newBuilder()
                        .setNum(std)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getFactors(CalculateRequest request, StreamObserver<CalculateResponse> responseObserver) {
        double sqrt = Math.sqrt(request.getNum());
        double currentValue = request.getNum();
        int multiplier = 2;
        while (currentValue > 1 && multiplier <= sqrt) {
            if (currentValue % multiplier == 0) {
                currentValue /= multiplier;
            } else if (multiplier == 2) {
                multiplier++;
            } else {
                multiplier += 2;
            }
        }
        if (currentValue != 1) {
            CalculateResponse response = CalculateResponse.newBuilder()
                    .setNum(currentValue)
                    .build();
            responseObserver.onNext(response);
        }
        if (currentValue <= 0) {
            responseObserver.onError(new IllegalArgumentException("Illegal value"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<CalculateRequest> getMaximum(StreamObserver<CalculateResponse> responseObserver) {
        return new StreamObserver<>() {
            private double max = Double.MIN_VALUE;

            @Override
            public void onNext(CalculateRequest calculateRequest) {
                final double number = calculateRequest.getNum();
                if (max < number) {
                    max = number;
                }
                responseObserver.onNext(CalculateResponse.newBuilder()
                        .setNum(max)
                        .build());

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}

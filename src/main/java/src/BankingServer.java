// package src;

// public class BankingServer {
//     public static void main(String[] args) throws Exception {
//         Server server = ServerBuilder.forPort(8080)
//                 .addService((BindableService) new BankingServiceImpl())
//                 .build();

//         server.start();
//         server.awaitTermination();
//     }

//     private static class BankingServiceImpl extends BankingServiceGrpc.BankingServiceImplBase {
//         @Override
//         public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
//             int key = request.getKey();
//             String password = request.getPassword();
//         }

//         @Override
//         public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
//             // Implement register logic here
//             // Call your existing registration method
//             // Use responseObserver to send the response back
//         }

//         @Override
//         public void getBalance(GetBalanceRequest request, StreamObserver<GetBalanceResponse> responseObserver) {
//             // Implement getBalance logic here
//             // Call your existing getBalance method
//             // Use responseObserver to send the response back
//         }

//         @Override
//         public void makeTransfer(MakeTransferRequest request, StreamObserver<MakeTransferResponse> responseObserver) {
//             // Implement makeTransfer logic here
//             // Call your existing makeTransfer method
//             // Use responseObserver to send the response back
//         }
//     }
// }
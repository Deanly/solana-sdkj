# Solana RPC SDK for Java 

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Documentation](https://img.shields.io/badge/API-Documentation-lightgrey)](https://docs.solana.com/apps/jsonrpc-api)
[![Maven Central](https://img.shields.io/maven-central/v/net.deanly/solana-sdkj)](https://search.maven.org/artifact/net.deanly/solana-sdkj)

`solana-sdkj` provides a flexible, developer-friendly way to integrate with the Solana blockchain ecosystem. It builds upon Solana RPC methods with a focus on supporting Java developers in creating applications with System Programs and other Solana-native programs. It aims to simplify working with Program States and enables easy customizability and extendability of Solana programs.

## ✨ Features

- Full support for **Solana RPC** API methods.
- Simplified handling of **System Programs** and state management.
- Extendable and customizable program definitions for easy integration.
- Built with pure Java and compatible with Java 17+.

---

## 📚 Table of Contents

- [Requirements](#%EF%B8%8F-requirements)
- [Dependencies](#-dependencies)
- [Installation](#%EF%B8%8F-installation)
- [Getting Started](#-getting-started)
  - [Retrieve Account Balance](#retrieve-account-balance)
  - [Transfer SOL (Lamports)](#transfer-sol-lamports)
  - [Simulate Transaction](#simulate-transaction)
- [License](#-license)

---

## 🛠️ Requirements

- Java 17 or higher.

---

## 📦 Dependencies

`solana-sdkj` depends on the following libraries:
- [OkHttp](https://square.github.io/okhttp/) – For making HTTP requests.
- [Moshi](https://github.com/square/moshi) – JSON serialization/deserialization library.
- [BouncyCastle](https://www.bouncycastle.org/) – For cryptographic operations.
- [Struct-layout](https://github.com/Deanly/struct-layout) - For Borsh data.

---

## 🏗️ Installation

You can include `solana-sdkj` in your project via Maven. Add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>net.deanly</groupId>
  <artifactId>solana-sdkj</artifactId>
  <version>0.0.1</version>
</dependency>
```

If you're using Gradle, add the following to your `build.gradle`:

```gradle
implementation 'net.deanly:solana-sdkj:0.0.1'
```

---

## 🚀 Getting Started

### Retrieve Account Balance

`solana-sdkj` lets you easily interact with Solana through the RPC API and built-in program abstractions. Here is a quick example:

```java
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.config.ClientConfig;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.types.PublicKey;

public class Example {
  public static void main(String[] args) throws Exception {
    RpcClient rpcClient = new RpcClient(ClientConfig.builder()
            .network(Network.DEVNET)
            .build());

    // Example: Retrieve Balance
    PublicKey publicKey = new PublicKey("YourPublicKeyHere");
    long balance = rpcClient.getRpcHttpApi().getBalance(publicKey);

    System.out.println("Balance: " + balance);
  }
}
```

### Transfer SOL (Lamports)

This example demonstrates how to transfer SOL (measured in Lamports) from one wallet to another.

```java
import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.config.ClientConfig;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.transaction.Transaction;

public class TransferExample {
  public static void main(String[] args) {
    // Configure Solana RPC
    RpcClient rpcClient = new RpcClient(ClientConfig.builder().network(Network.DEVNET).build());

    try {
      // Account setup
      PublicKey senderPublicKey = new PublicKey("YOUR_SENDER_PUBLIC_KEY");
      PublicKey receiverPublicKey = new PublicKey("YOUR_RECEIVER_PUBLIC_KEY");
      KeyPair senderKeyPair = new KeyPair("YOUR_SENDER_PRIVATE_KEY_IN_BASE58".getBytes());

      // Amount to transfer (in lamports, 1 SOL = 10^9 lamports)
      long lamports = 1000000L;

      // Build transaction
      Transaction transaction = new Transaction();
      transaction.addInstruction(
              SystemProgram.transfer(senderPublicKey, receiverPublicKey, lamports)
      );
      transaction.setSigner(senderKeyPair);

      // Send transaction
      String transactionSignature = rpcClient.getRpcHttpApi().sendTransaction(transaction).getValue();
      System.out.println("Transaction sent successfully! Signature: " + transactionSignature);
    } catch (Exception e) {
      System.err.println("Failed to complete transfer: " + e.getMessage());
    }
  }
}
```

### Simulate Transaction

Use the `simulateTransaction` RPC method to test the validity of a Solana transaction without executing it on-chain.

```java
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.config.ClientConfig;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.crypto.KeyPair;
import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.transaction.Transaction;
import net.deanly.solana.sdk.transaction.instruction.TransactionInstruction;
import net.deanly.solana.sdk.program.core.system.SystemProgram;
import net.deanly.solana.sdk.types.Blockhash;
import net.deanly.solana.sdk.rpc.request.config.SimulateTransactionConfig;
import net.deanly.solana.sdk.rpc.response.ResValueSimulatedTransaction;
import net.deanly.structlayout.StructLayout;

public class SimulateTransactionExample {
  public static void main(String[] args) {
    // Configure Solana RPC
    RpcClient rpcClient = new RpcClient(ClientConfig.builder().network(Network.DEVNET).build());

    try {
      // Fetch recent blockhash
      Blockhash recentBlockhash = rpcClient.getRpcHttpApi()
              .getLatestBlockhash()
              .getValue()
              .getBlockhash();

      // Account setup
      PublicKey senderPublicKey = new PublicKey("YOUR_SENDER_PUBLIC_KEY");
      PublicKey receiverPublicKey = new PublicKey("YOUR_RECEIVER_PUBLIC_KEY");
      KeyPair senderKeyPair = new KeyPair("YOUR_SENDER_PRIVATE_KEY_IN_BASE58".getBytes());

      // Transaction setup
      long lamports = 100000L; // Amount in lamports
      Transaction transaction = new Transaction();
      transaction.setRecentBlockhashForCompile(recentBlockhash);
      transaction.addInstruction(
              SystemProgram.transfer(senderPublicKey, receiverPublicKey, lamports)
      );
      transaction.sign(senderKeyPair);

      // Simulate transaction
      var config = SimulateTransactionConfig.builder().skipPreflight(true).build();
      ResValueSimulatedTransaction simulateResponse = rpcClient.getRpcHttpApi()
              .simulateTransaction(transaction, config).getValue();

      // Print results
      StructLayout.debug(transaction);
      System.out.println("Simulation Result: " + simulateResponse);
    } catch (Exception e) {
      System.err.println("Simulation failed: " + e.getMessage());
    }
  }
}
```

The `simulateTransaction` function allows developers to test transactions before committing them to the blockchain. The result will indicate if the transaction would succeed or fail and, if it fails, what issues to address.

---
🚧 Difference from solanaj

`solanaj`, developed by Michael Morrell, laid a solid foundation for Java-based Solana development.   
`solana-sdkj` enhances and extends this foundation, introducing additional features and optimizations for modern Java applications:
- Uses BouncyCastle for enhanced cryptographic security.
- Simplifies Borsh serialization with Struct-layout for better maintainability.
- Enhances type safety in RPC APIs using generics and stricter type definitions.
- Provides well-structured classes for configurations and data handling.
- Redesigns communication logic with improved support for HTTP and WebSocket interactions.
- Extends transaction compilation with MessageV0 support, ensuring compatibility with Solana’s evolving architecture.
- Refactors the codebase for improved maintainability, extensibility, and performance.

We recognize the contributions of `solanaj` and its community, and our goal is to extend its capabilities while introducing enhancements tailored for Java developers.

---

## 📄 License

`solana-sdkj` is open-source software licensed under the [MIT License](LICENSE).

This project expands upon the groundwork laid by `solanaj`, bringing architectural improvements and expanded functionality while ensuring compatibility with the Solana ecosystem.
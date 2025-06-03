# Solana SDK for Java (`solana-sdkj`)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Documentation](https://img.shields.io/badge/API-Documentation-lightgrey)](https://docs.solana.com/apps/jsonrpc-api)
[![Maven Central](https://img.shields.io/maven-central/v/net.deanly/solana-sdkj)](https://search.maven.org/artifact/net.deanly/solana-sdkj)

`solana-sdkj` is a modern, developer-friendly SDK for building Java applications on the Solana blockchain.  
It provides complete RPC access, convenient abstractions for System Programs, and first-class support for Borsh-encoded program states using `struct-layout`.

---

## ✨ Features

- Full support for all **Solana RPC** methods
- Built-in abstractions for **SystemProgram**, **Transactions**, **Program Derived Addresses**, and **VersionedMessage** (v0)
- Strong **Borsh/Rust/C state decoding** via `@StructLayout`  
  Easily map program account data into structured Java classes  
  Extend the `State` class and pair it with `getAccountState()` to decode any base64-encoded account with full type safety
- Safer and more maintainable than `jsonParsed` — no runtime casting, no fragile field access.
- Clean interoperability with custom programs and PDA-based layouts
- Java 17+ compatible, with secure cryptography powered by **BouncyCastle**

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
  <version>0.1.0</version>
</dependency>
```

If you're using Gradle, add the following to your `build.gradle`:

```gradle
implementation 'net.deanly:solana-sdkj:0.1.0'
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


### Read Account State via StructLayout (TokenMetadata Example)

You can decode a Solana account’s data into a structured class (State) using getAccountState. This makes it easy to work with Borsh-encoded on-chain data, such as Metaplex’s Token Metadata.
```java
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.config.ClientConfig;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.types.PublicKey;
import net.deanly.solana.sdk.types.ProgramDerivedAddress;
import net.deanly.solana.sdk.rpc.response.ResValueAccountInfo;
import net.deanly.solana.sdk.rpc.request.config.AccountInfoConfig;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.state.TokenMetadataState;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReadTokenMetadataExample {
  public static void main(String[] args) throws Exception {
    RpcClient rpcClient = new RpcClient(ClientConfig.builder()
            .network(Network.MAINNET)
            .build());

    // Target Mint Address
    String mintBase58 = "6p6xgHyF7AeE6TZkSmFsko444wqoP15icUSqi2jfGiPN";
    PublicKey mint = PublicKey.valueOf(mintBase58);

    // Metaplex Token Metadata Program ID
    PublicKey metadataProgramId = TokenMetadataProgram.PROGRAM_ID;

    // Derive Metadata PDA
    List<byte[]> seeds = List.of(
            "metadata".getBytes(StandardCharsets.UTF_8),
            metadataProgramId.toByteArray(),
            mint.toByteArray()
    );
    ProgramDerivedAddress pda = PublicKey.findProgramAddress(seeds, metadataProgramId);
    System.out.println("Metadata PDA: " + pda.getAddress().toBase58());

    // Fetch and decode state directly
    TokenMetadataState state = rpcClient.getRpcHttpApi().getAccountState(pda.getAddress(), TokenMetadataState.class);

    System.out.println("Decoded Token Metadata:");
    System.out.println(state);
  }
}
```
Sample Output
```
TokenMetadataState(
  key=4,
  updateAuthority=5e2qRc1DNEXmyxP8qwPwJhRWjef7usLyi7v5xjqLr5G7,
  mint=6p6xgHyF7AeE6TZkSmFsko444wqoP15icUSqi2jfGiPN,
  name=OFFICIAL TRUMP,
  symbol=TRUMP,
  uri=https://arweave.net/cSCP0h2n1crjeSWE9KF-XtLciJalDNFs7Vf-Sm0NNY0,
  sellerFeeBasisPoints=0,
  creators=[...],
  primarySaleHappened=false,
  isMutable=false,
  ...
)
```
`getAccountState` automatically decodes Borsh-encoded data using the class you provide (which must extend State). You can define your own `@StructLayout-annotated` types to read custom program accounts.

### Read Token Accounts via JSON_PARSED

You can also access raw JSON-parsed account data without defining a typed model, using key-path access:

```java
public static void sample() {
  var address = net.deanly.solana.sdk.crypto.PublicKey.valueOf("");

  RpcResultObject<List<ResValueTokenAccount>> result = rpc.getRpcHttpApi()
          .getTokenAccountsByOwner(
                  address,
                  TokenAccountByOwnerFilter.builder()
                          .programId(SplTokenProgram.PROGRAM_ID)
                          .build(),
                  TokenAccountsByOwnerConfig.builder()
                          .encoding(Encoding.JSON_PARSED)
                          .build());

  for (ResValueTokenAccount value : result.getValue()) {
    String mint = (String) value.getAccount().getData().getObjectValue("parsed.info.mint");
    String amount = (String) value.getAccount().getData().getObjectValue("parsed.info.tokenAmount.uiAmountString");
    System.out.println("Mint: " + mint + ", Amount: " + amount);
  }
}
```


---

## 🤝 Acknowledgement

This project is inspired by solanaj by Michael Morrell.
We appreciate the groundwork laid by its contributors and aim to continue that spirit with an extended and modernized architecture tailored for today’s Java developers.

---

## 📄 License

Licensed under thee [MIT License](LICENSE).


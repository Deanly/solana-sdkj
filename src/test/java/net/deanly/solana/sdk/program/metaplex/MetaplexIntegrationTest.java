package net.deanly.solana.sdk.program.metaplex;

import net.deanly.solana.sdk.crypto.PublicKey;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.TokenMetadataProgram;
import net.deanly.solana.sdk.program.metaplex.tokenmetadata.state.TokenMetadataState;
import net.deanly.solana.sdk.program.pda.ProgramDerivedAddress;
import net.deanly.solana.sdk.rpc.client.RpcClient;
import net.deanly.solana.sdk.rpc.client.config.Network;
import net.deanly.solana.sdk.rpc.client.exception.RpcException;
import net.deanly.solana.sdk.rpc.request.config.AccountInfoConfig;
import net.deanly.solana.sdk.rpc.response.ResValueAccountInfo;
import net.deanly.solana.sdk.rpc.response.RpcResultObject;
import net.deanly.solana.sdk.types.Encoding;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.exception.StructDecodingException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

// https://github.com/metaplex-foundation/mpl-token-metadata
public class MetaplexIntegrationTest {

    RpcClient client = new RpcClient(Network.MAINNET);

    @Test
    public void testMainnetRpc() throws RpcException {
        String mintBase58 = "6p6xgHyF7AeE6TZkSmFsko444wqoP15icUSqi2jfGiPN";
        PublicKey mint = PublicKey.valueOf(mintBase58);

        // Metaplex Token Metadata Program ID (Mainnet 기준)
        PublicKey metadataProgramId = TokenMetadataProgram.PROGRAM_ID;

        // Seed 정의 (주의: "metadata"는 ASCII byte)
        List<byte[]> seeds = List.of(
                "metadata".getBytes(StandardCharsets.UTF_8),
                metadataProgramId.toByteArray(),
                mint.toByteArray()
        );

        // PDA 계산
        ProgramDerivedAddress pda = PublicKey.findProgramAddress(seeds, metadataProgramId);
        System.out.println("📍 Metadata PDA: " + pda.getAddress().toBase58());
        System.out.println("🔁 Nonce used: " + pda.getNonce());

        RpcResultObject<ResValueAccountInfo> result = this.client.getRpcHttpApi().getAccountInfo(
                pda.getAddress(),
                AccountInfoConfig.builder().encoding(Encoding.BASE64).build()
        );

        System.out.println(result);

    }

    @Test
    public void parseBase64StateData() {
//        String base64Data = "BKG2W9DEXoENxmfkQas+ol7/sWV4vT1uXdNJATG898oLahkMzlxUQmTeGNZuHQN+QgYmBd5ge1cuU9Id4QTA1oggAAAAVEFDT0NBVAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKAAAAVEFDT0NBVAAAAMgAAABodHRwczovL25mdHN0b3JhZ2UubGluay9pcGZzL2JhZmtyZWlmNGJpeGQ1d2VkZHJramFrNnh4NWNvc3E0bWtsaXN4ZHZtZGV0ejI3MmNoc2M1NHZpbGk0AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAf8BAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
        String base64Data = "BETrT/dtjSPFRvGJb3Advxr7KXOmpFKv2ekTbOJdPc3UVlt4uuxb1v8GYzMY6iDn9jmNLzKA6Pf4w4BOKI54p40gAAAAT0ZGSUNJQUwgVFJVTVAAAAAAAAAAAAAAAAAAAAAAAAAKAAAAVFJVTVAAAAAAAMgAAABodHRwczovL2Fyd2VhdmUubmV0L2NTQ1AwaDJuMWNyamVTV0U5S0YtWHRMY2lKYWxETkZzN1ZmLVNtME5OWTAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQEAAABE60/3bY0jxUbxiW9wHb8a+ylzpqRSr9npE2ziXT3N1AFkAAAB/wECAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
        byte[] bytesData = Base64.getDecoder().decode(base64Data);

        StructLayout.debug(bytesData);
        try {
            TokenMetadataState tokenMetadata = StructLayout.decode(bytesData, TokenMetadataState.class);
            System.out.println(tokenMetadata);

        } catch (StructDecodingException e) {
            System.out.println(e.getDebugLog());
            throw e;
        }

    }
}

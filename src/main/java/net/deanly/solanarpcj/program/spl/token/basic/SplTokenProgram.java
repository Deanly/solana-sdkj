package net.deanly.solanarpcj.program.spl.token.basic;

import lombok.Getter;
import lombok.NonNull;
import net.deanly.solanarpcj.crypto.PublicKey;
import net.deanly.solanarpcj.layout.Struct;
import net.deanly.solanarpcj.program.spl.token.basic.instruction.*;
import net.deanly.solanarpcj.program.spl.token.basic.type.AuthorityType;
import net.deanly.solanarpcj.transaction.instruction.AccountMeta;

import java.util.List;

// https://github.com/solana-program/token/blob/main/program/src/instruction.rs
public class SplTokenProgram {

    public static final PublicKey PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");

    @Getter
    public static class Base extends Struct {
        private final PublicKey programId = PROGRAM_ID;
    }

    public static TokenInstruction00InitializeMint initializeMint(
            @NonNull PublicKey mint,
            @NonNull PublicKey mintAuthority,
            int decimals,
            PublicKey freezeAuthority,
            PublicKey rentAccount
    ) {
        return TokenInstruction00InitializeMint.create(mint, mintAuthority, decimals, freezeAuthority, rentAccount);
    }

    public static TokenInstruction01InitializeAccount initializeAccount(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey owner,
            PublicKey rent,
            List<PublicKey> additionalAccounts
    ) {
        return TokenInstruction01InitializeAccount.create(account, mint, owner, rent, additionalAccounts);
    }

    public static TokenInstruction02InitializeMultisig initializeMultisig(
            @NonNull PublicKey multisigAccount,
            @NonNull List<PublicKey> signerKeys,
            PublicKey rent
    ) {
        return TokenInstruction02InitializeMultisig.create(multisigAccount, signerKeys, rent);
    }

    public static TokenInstruction03Transfer transfer(
            @NonNull PublicKey source,
            @NonNull PublicKey destination,
            @NonNull PublicKey authority,
            long amount,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction03Transfer.create(source, destination, authority, amount, multiSigners);
    }

    public static TokenInstruction04Approve approve(
            @NonNull PublicKey source,
            @NonNull PublicKey delegate,
            @NonNull PublicKey owner,
            long amount,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction04Approve.create(source, delegate, owner, amount, multiSigners);
    }

    public static TokenInstruction05Revoke revoke(
            @NonNull PublicKey source,
            @NonNull PublicKey owner,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction05Revoke.create(source, owner, multiSigners);
    }

    public static TokenInstruction06SetAuthority setAuthority(
            @NonNull PublicKey account,
            @NonNull PublicKey owner,
            @NonNull AuthorityType authorityType,
            PublicKey newAuthority,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction06SetAuthority.create(account, owner, authorityType, newAuthority, multiSigners);
    }

    public static TokenInstruction07MintTo mintTo(
            @NonNull PublicKey mint,
            @NonNull PublicKey destination,
            @NonNull PublicKey mintAuthority,
            long amount,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction07MintTo.create(mint, destination, mintAuthority, amount, multiSigners);
    }

    public static TokenInstruction08Burn burn(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey authority,
            long amount,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction08Burn.create(account, mint, authority, amount, multiSigners);
    }

    public static TokenInstruction09CloseAccount closeAccount(
            @NonNull PublicKey accountToClose,
            @NonNull PublicKey destination,
            @NonNull PublicKey owner,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction09CloseAccount.create(accountToClose, destination, owner, multiSigners);
    }

    public static TokenInstruction10FreezeAccount freezeAccount(
            @NonNull PublicKey accountToFreeze,
            @NonNull PublicKey mint,
            @NonNull PublicKey freezeAuthority,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction10FreezeAccount.create(accountToFreeze, mint, freezeAuthority, multiSigners);
    }

    public static TokenInstruction11ThawAccount thawAccount(
            @NonNull PublicKey accountToThaw,
            @NonNull PublicKey mint,
            @NonNull PublicKey freezeAuthority,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction11ThawAccount.create(accountToThaw, mint, freezeAuthority, multiSigners);
    }

    public static TokenInstruction12TransferChecked transferChecked(
            @NonNull PublicKey source,
            @NonNull PublicKey destination,
            @NonNull PublicKey mint,
            @NonNull PublicKey authority,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction12TransferChecked.create(source, destination, mint, authority, amount, decimals, multiSigners);
    }

    public static TokenInstruction13ApproveChecked approveChecked(
            @NonNull PublicKey source,
            @NonNull PublicKey delegate,
            @NonNull PublicKey mint,
            @NonNull PublicKey owner,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction13ApproveChecked.create(source, mint, delegate, owner, amount, decimals, multiSigners);
    }

    public static TokenInstruction14MintToChecked mintToChecked(
            @NonNull PublicKey mint,
            @NonNull PublicKey token,
            @NonNull PublicKey mintAuthority,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction14MintToChecked.create(mint, token, mintAuthority, amount, decimals, multiSigners);
    }

    public static TokenInstruction15BurnChecked burnChecked(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey authority,
            long amount,
            int decimals,
            List<PublicKey> multiSigners
    ) {
        return TokenInstruction15BurnChecked.create(account, mint, authority, amount, decimals, multiSigners);
    }

    public static TokenInstruction16InitializeAccount2 initializeAccount2(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey owner,
            PublicKey rent
    ) {
        return TokenInstruction16InitializeAccount2.create(account, mint, owner, rent);
    }

    public static TokenInstruction17SyncNative syncNative(@NonNull PublicKey nativeAccount) {
        return TokenInstruction17SyncNative.create(nativeAccount);
    }

    public static TokenInstruction18InitializeAccount3 initializeAccount3(
            @NonNull PublicKey account,
            @NonNull PublicKey mint,
            @NonNull PublicKey owner
    ) {
        return TokenInstruction18InitializeAccount3.create(account, mint, owner);
    }

    public static TokenInstruction19InitializeMultisig2 initializeMultisig2(
            @NonNull PublicKey multisig,
            int m,
            @NonNull List<PublicKey> signers
    ) {
        return TokenInstruction19InitializeMultisig2.create(multisig, m, signers);
    }

    public static TokenInstruction20InitializeMint2 initializeMint2(
            @NonNull PublicKey mint,
            int decimals,
            @NonNull PublicKey mintAuthority,
            PublicKey freezeAuthority
    ) {
        return TokenInstruction20InitializeMint2.create(mint, decimals, mintAuthority, freezeAuthority);
    }

    public static TokenInstruction21GetAccountDataSize getAccountDataSize(@NonNull PublicKey mint) {
        return TokenInstruction21GetAccountDataSize.create(mint);
    }

    public static TokenInstruction22InitializeImmutableOwner initializeImmutableOwner(@NonNull PublicKey account) {
        return TokenInstruction22InitializeImmutableOwner.create(account);
    }

    public static TokenInstruction23AmountToUiAmount amountToUiAmount(
            @NonNull PublicKey mint,
            long amount
    ) {
        return TokenInstruction23AmountToUiAmount.create(mint, amount);
    }

    public static TokenInstruction24UiAmountToAmount uiAmountToAmount(
            @NonNull PublicKey mint,
            @NonNull String uiAmount
    ) {
        return TokenInstruction24UiAmountToAmount.create(mint, uiAmount);
    }
}

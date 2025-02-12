# SPL 프로그램 패키지 구조

## 📌 개요
각 패키지는 **SPL에서 제공하는 주요 기능별로 그룹핑**되었음

---

## 📂 **패키지 구조**
```plaintext
net.deanly.solana.spl.token                        # SPL-Token 기본 기능
- Program ID: `TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA`

net.deanly.solana.spl.ata                          # Associated Token Account (ATA) 관련 기능
- Program ID: `ATokenGPvR93bVnK1W8Yf7z2Rz3Wf6r85iE8HNojPZ`

net.deanly.solana.spl.tokenswap                    # SPL-Token 기반 AMM
- Program ID: `SwaPpHr9gP4sZkYkYkYkYkYkYkYkYkYkYkYkYkYkYk`

net.deanly.solana.spl.tokenlending                 # SPL-Lending 프로그램 관련 기능
- Program ID: `LendZqTs7gn5CTSJU1jWKhKuVpjJGom45nnwPb2AMTi`

net.deanly.solana.spl.tokenext                     # Confidential Token Extension 기능
- Program ID: `Conf1d3nt1aLToKenExtens1on111111111111111`

net.deanly.solana.spl.tokentransfer                # Token Transfer Hook 관련 기능
- Program ID: `HookTr4nsf3r111111111111111111111111111111`

net.deanly.solana.spl.memo                         # SPL-Memo 프로그램
- Program ID: `MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr`

net.deanly.solana.spl.nameservice                  # SPL-Name Service (ENS와 유사한 기능)
- Program ID: `namesLP1s1s1s1s1s1s1s1s1s1s1s1s1s1s1s1s1s`

net.deanly.solana.spl.stakepool                    # Staking Pool 관련 기능
- Program ID: `SPoo1Ku8WFXoNDMHPsrGSTSG1Y47rzgn41SLUNakuHy`

net.deanly.solana.spl.featureproposal              # Feature Proposal 관련 기능 (거버넌스 포함)
- Program ID: `Feat1urePropos4l111111111111111111111111111`

net.deanly.solana.spl.accountcompression           # Account Compression (상태 압축 저장) 관련 기능
- Program ID: `AcctCompress1on1111111111111111111111111111`
```

# SPL 프로그램 패키지 구조

## 📌 개요
각 패키지는 **SPL에서 제공하는 주요 기능별로 그룹핑**되었음

---

## 📂 **패키지 구조**
```plaintext
.spl.token.basic                 # SPL-Token 기본 기능
- Program ID: `TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA`

.spl.token.associated            # Associated Token Account (ATA) 관련 기능
- Program ID: `ATokenGPvR93bVnK1W8Yf7z2Rz3Wf6r85iE8HNojPZ`

.spl.token.swap                  # SPL-Token 기반 AMM (예: Raydium, Serum)
- Program ID: `SwaPpHr9gP4sZkYkYkYkYkYkYkYkYkYkYkYkYkYkYk`

.spl.token.lending               # SPL-Lending 프로그램 관련 기능
- Program ID: `LendZqTs7gn5CTSJU1jWKhKuVpjJGom45nnwPb2AMTi`

.spl.token.extension.confidential  # Confidential Token Extension 기능
- Program ID: `Conf1d3nt1aLToKenExtens1on111111111111111`

.spl.token.hook.transfer         # Token Transfer Hook 관련 기능
- Program ID: `HookTr4nsf3r111111111111111111111111111111`

.spl.memo                        # SPL-Memo 프로그램
- Program ID: `MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr`

.spl.name.service                # SPL-Name Service (ENS와 유사한 기능)
- Program ID: `namesLP1s1s1s1s1s1s1s1s1s1s1s1s1s1s1s1s1s`

.spl.pool.stake                  # Staking Pool 관련 기능
- Program ID: `SPoo1Ku8WFXoNDMHPsrGSTSG1Y47rzgn41SLUNakuHy`

.spl.feature.proposal            # Feature Proposal 관련 기능 (거버넌스 포함)
- Program ID: `Feat1urePropos4l111111111111111111111111111`

.spl.KeyPair.compression         # Account Compression (상태 압축 저장) 관련 기능
- Program ID: `AcctCompress1on1111111111111111111111111111`
```

[![CITA](https://img.shields.io/badge/made%20for-CITA-blue.svg)](https://www.citahub.com)

# Cyton Wallet (Android)

Overview
===============

Cyton is an open source blockchain wallet which supports Ethereum and [CITA](https://citahub.com/#/). It supports most tokens of Ethereum and [CITA](https://docs.nervos.org/#/), such as ETH, ERC20, ERC721, and also supports most kinds of DApps of Ethereum and [CITA](https://docs.nervos.org/#/) , such as cryptokitties, Fomo3D, 0xproject...

Usage
===============

### Private key and address

Cyton is a blockchain wallet which supports Ethereum and [CITA](https://citahub.com/#/) (CITA uses same SECP256k1 signature algorithm), you can use same private key and address. **Cyton never saves your private key directly, you need to input password to sign every transaction. If you forget your private key, Cyton can not find and recover it, so you should save private key (keystore and mnemonic) carefully.**

Cyton supports importing wallet through private key, keystore and mnemonic, and supports exporting keystore.

### Token

Cyton is a blockchain wallet which supports Ethereum, so you can visit balance of most tokens of Ethereum and tranfer tokens to other accounts. If you can not find ERC20 token you want, you can input contract address to load token information and add to your token list.

[CITA](https://citahub.com/#/) is a blockchain solution which includes blockchain kernel CITA, Cyton wallet, blockchain browser [Microscope](https://github.com/cryptape/microscope), cache server [ReBirth](https://github.com/cryptape/re-birth) and SDKs. [CITA](https://docs.nervos.org/#/) supports Ethereum solidity contract, so all ERC contracts can run on CITA directly.

CITA is an open source blockchain solution, so you can publish your blockchain coin by yourself and set any name you like. All tokens on CITA can display in Cyton.

### DApp

Cyton is also a DApp browser, which supprts DApps of Ethereum and [CITA](https://citahub.com/#/). Most popular Ethereum DApps, such as cryptokitties, Fomo3D, 0xproject, can run in Cyton directly. Cyton also supports CITA DApps, which can be easily migrated from Ethereum. You can get more information about [how to develop an CITA DApp](https://docs.nervos.org/nervos-appchain-docs/#/quick-start/build-dapp).

Getting Started
===============

### Open from source code

1. Clone this repo to your machine
2. Download and install [Android Studio](https://developer.android.com/studio/index.html)
3. Open this project in Android Studio (Choosing the contained directory in Android Studio `Open` dialog works)
4. Click the `Play` button

### Install from apk file

You can download apk file from [release](https://github.com/cryptape/Cyton-android/releases) and install directly on Andorid smart phone.

Making a new build for App store (Dev only)
============================================

Cyton is an open source blockchain wallet, so you can use source code to build your wallet by yourself, and you can edit source code by yourself. You can start your tour from here: 

1. Clone source code to local
2. Open config.gradle
3. Increase the versionCode by 1.
4. Build the APK however you want (in Android Studio or via gradle)
    - You'll need the release certificate, alias and password.
5. Commit the changes to build.gradle, upload the APKs to App store

Contributing
============================================

We intend for this project to be an open source resource: we are excited to
share our wins, mistakes, and methodology of Android development as we work
in the open. Our primary focus is to continue improving the app for our users in
line with our roadmap.

The best way to submit feedback and report bugs is to open a GitHub issue.
Please be sure to include your operating system, device, version number, and
steps to reproduce reported bugs. Keep in mind that all participants will be
expected to follow our code of conduct.

MIT License
============================================
Cyton is open sourced under MIT License.

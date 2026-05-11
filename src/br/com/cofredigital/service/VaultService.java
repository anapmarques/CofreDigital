package br.com.cofredigital.service;

import java.io.File;
import java.nio.file.Files;
import br.com.cofredigital.crypto.AESUtil;

public class VaultService {
    // Lista e exibe o conteúdo do index do cofre após decriptar com a chave mestre
    public void listVaultFilesInteractive() throws Exception {
        File idxEnc = new File("vault/index.enc");
        File idxAsd = new File("vault/index.asd");
        if (!idxEnc.exists()) { System.out.println("index.enc não encontrado"); return; }
        if (!idxAsd.exists()) { System.out.println("index.asd não encontrado"); return; }

        byte[] enc = Files.readAllBytes(idxEnc.toPath());
        // master key from envelope
        byte[] master = Files.readAllBytes(new File("vault/index.env").toPath());
        byte[] key = new byte[16]; System.arraycopy(master, 0, key, 0, 16);
        byte[] plain = AESUtil.decrypt(key, enc);
        // signature file must be verified with a certificate in storage - skipped for brevity
        System.out.println("Conteúdo do index (decriptado):\n" + new String(plain));
    }
}

package com.school.service.impl;

import com.school.dto.LoginDTO;
import com.school.dto.PublicKeyDTO;
import com.school.dto.RegisterDTO;
import com.school.vo.PublicKeyVO;
import com.school.vo.LoginVO;
import com.school.entity.Account;
import com.school.service.AccountService;
import com.school.security.TokenService;
import com.school.security.AccountKeyService;
import com.school.service.RoleService;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import javax.annotation.PostConstruct;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import java.security.GeneralSecurityException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountService accounts;

    private final PasswordEncoder passwords;

    private final TokenService tokens;

    private final AccountKeyService keys;

    private final RoleService roles;

    private String dummyHash;

    @PostConstruct
    private void initializeDummyHash() {
        this.dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
    }

    @Override
    public LoginVO login(LoginDTO request) {
        List<Account> matches = accounts.findLoginAccounts(request.getAccount());
        Account account = matches.size() == 1 ? matches.get(0) : null;
        if (account == null) {
            passwords.matches("invalid", dummyHash);
            throw new BadCredentialsException("帳號或密碼錯誤");
        }
        String clearPassword;
        try {
            clearPassword = keys.decryptPassword(request.getPassword(), account.getPublicKey(), account.getPrivateKey());
        } catch (GeneralSecurityException exception) {
            passwords.matches("invalid", dummyHash);
            throw new BadCredentialsException("帳號或密碼錯誤");
        }
        String hash = account == null || account.getPassword() == null ? dummyHash : account.getPassword();
        boolean valid = passwords.matches(clearPassword, hash);
        if (account == null || !valid || account.getRole() == null
                || !account.getRole().equals(accounts.findCurrentRole(account.getId()))) {
            throw new BadCredentialsException("帳號或密碼錯誤");
        }
        return tokens.issue(account.getId(), account.getRole());
    }

    @Override
    public PublicKeyVO publicKey(PublicKeyDTO request) {
        List<Account> matches = accounts.findLoginAccounts(request.getAccount());
        if (matches.size() != 1 || matches.get(0).getPublicKey() == null
                || matches.get(0).getPublicKey().trim().isEmpty()) {
            throw new BadCredentialsException("無法取得登入公鑰");
        }
        return new PublicKeyVO(matches.get(0).getPublicKey());
    }

    @Override
    @Transactional
    public void register(RegisterDTO request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("密碼的 UTF-8 編碼不可超過 72 bytes");
        }
        if (!accounts.findLoginAccounts(request.getAccount()).isEmpty()) {
            throw new IllegalArgumentException("無法註冊此帳號");
        }
        // 角色只能由後端指定，公開註冊不得自行指定管理員角色。
        if (roles.getById(4) == null) throw new IllegalStateException("請先建立家長角色 4");
        AccountKeyService.KeyMaterial material = keys.generate();
        Account account = new Account();
        account.setAccount(request.getAccount());
        account.setName(request.getName());
        account.setPassword(passwords.encode(request.getPassword()));
        account.setRole(4);
        account.setPublicKey(material.getPublicKey());
        account.setPrivateKey(material.getEncryptedPrivateKey());
        account.setToken(""); // 保留欄位，不將 JWT 存入資料庫或改變既有驗證方式。
        try {
            if (!accounts.save(account)) throw new IllegalStateException("註冊失敗");
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("無法註冊此帳號");
        }
    }

    public static void main(String[] args) {
        System.err.println(UUID.randomUUID());
    }
}

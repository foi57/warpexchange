package com.itranswarp.exchange.user;

import com.itranswarp.exchange.ApiError;
import com.itranswarp.exchange.ApiException;
import com.itranswarp.exchange.enums.UserType;
import com.itranswarp.exchange.model.ui.PasswordAuthEntity;
import com.itranswarp.exchange.model.ui.UserEntity;
import com.itranswarp.exchange.model.ui.UserProfileEntity;
import com.itranswarp.exchange.support.AbstractDbService;
import com.itranswarp.exchange.util.HashUtil;
import com.itranswarp.exchange.util.RandomUtil;
import jakarta.annotation.Nullable;
import org.apache.catalina.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UserService extends AbstractDbService {

    public UserProfileEntity getUserProfile(Long userId) {return db.get(UserProfileEntity.class,userId);}

    @Nullable
    public UserProfileEntity fetchUserProfileByEmail(String email) {
        UserProfileEntity userProfile = fetchUserProfileByEmail(email);
        if (userProfile == null) {
            throw new ApiException(ApiError.AUTH_SIGNIN_FAILED);
        }
        return userProfile;
    }

    public UserProfileEntity getUserProfileByEmail(String email){
        UserProfileEntity userProfile = fetchUserProfileByEmail(email);
        if (userProfile == null) {
            throw new ApiException(ApiError.AUTH_SIGNIN_FAILED);
        }
        return userProfile;
    }

    public UserProfileEntity signup(String email, String name,String passwd) {
        final  long ts = System.currentTimeMillis();
        var user = new UserEntity();
        user.type = UserType.TRADER;
        user.createdAt = ts;
        db.insert(user);
        var up = new UserProfileEntity();
        up.userId = user.id;
        up.email = email;
        up.name = name;
        up.createdAt = up.updatedAt =  ts;
        db.insert(up);
        var pa = new PasswordAuthEntity();
        pa.userId = user.id;
        pa.random = RandomUtil.createRandomString(32);
        pa.passwd = HashUtil.hmacSha256(passwd,pa.random);
        db.insert(pa);
        return up;
    }

    public UserProfileEntity signin(String email,String passwd){
        UserProfileEntity userProfile = getUserProfileByEmail(email);
        PasswordAuthEntity pa =db.fetch(PasswordAuthEntity.class,userProfile.userId);
        if (pa == null){
            throw new ApiException(ApiError.USER_CANNOT_SIGNIN);
        }
        String hash = HashUtil.hmacSha256(passwd,pa.random);
        if (!hash.equals(pa.passwd)){
            throw new ApiException(ApiError.AUTH_SIGNIN_FAILED);
        }
        return userProfile;
    }
}

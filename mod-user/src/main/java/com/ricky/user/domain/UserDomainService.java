package com.ricky.user.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ricky.common.exception.ErrorCodeEnum.USER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS;
import static com.ricky.common.utils.CommonUtils.isMobileNumber;
import static com.ricky.common.utils.CommonUtils.maskMobileOrEmail;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;

    public CreateUserResult register(String mobileOrEmail, String password, String username, UserContext userContext) {
        if (userRepository.existsByMobileOrEmail(mobileOrEmail)) {
            throw new MyException(USER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS, "注册失败，手机号或邮箱已被占用。",
                    "mobileOrEmail", maskMobileOrEmail(mobileOrEmail));
        }

        String mobile = null;
        String email = null;
        if (isMobileNumber(mobileOrEmail)) {
            mobile = mobileOrEmail;
        } else {
            email = mobileOrEmail;
        }

        return userFactory.create(username, mobile, email, password, userContext);
    }

    // 使用REQUIRES_NEW保证即便其他地方有异常，这里也能正常写库
    @Transactional(propagation = REQUIRES_NEW)
    public void recordUserFailedLogin(User user) {
        user.recordFailedLogin();
        userRepository.save(user);
    }

}

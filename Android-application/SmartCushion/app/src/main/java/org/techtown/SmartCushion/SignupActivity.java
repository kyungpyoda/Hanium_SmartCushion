/*
  Copyright 2014-2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.techtown.SmartCushion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.auth.ApiResponseCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 유효한 세션이 있다는 검증 후
 * me를 호출하여 가입 여부에 따라 가입 페이지를 그리던지 Main 페이지로 이동 시킨다.
 */
public class SignupActivity extends BaseActivity {
    /**
     * Main으로 넘길지 가입 페이지를 그릴지 판단하기 위해 me를 호출한다.
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("TEST", "signuponcreate");
        super.onCreate(savedInstanceState);

        requestMe();
    }

    protected void showSignup() {
        setContentView(R.layout.layout_usermgmt_signup);
        final ExtraUserPropertyLayout extraUserPropertyLayout = findViewById(R.id.extra_user_property);
        Button signupButton = findViewById(R.id.buttonSignup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSignUp(extraUserPropertyLayout.getProperties());
            }
        });
    }

    private void requestSignUp(final Map<String, String> properties) {
        UserManagement.getInstance().requestSignup(new ApiResponseCallback<Long>() {
            @Override
            public void onNotSignedUp() {
            }

            @Override
            public void onSuccess(Long result) {
                Log.d("TEST", "requestSignUp-onSuccess");
                requestMe();
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                final String message = "UsermgmtResponseCallback : failure : " + errorResult;
                com.kakao.util.helper.log.Logger.w(message);
                KakaoToast.makeToast(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
            }
        }, properties);
    }

    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void requestMe() {
        Log.d("TEST", "requestMe");
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);

                int result = errorResult.getErrorCode();
                if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                    KakaoToast.makeToast(getApplicationContext(), getString(R.string.error_message_for_service_unavailable), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    redirectLoginActivity();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Logger.e("onSessionClosed");
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(MeV2Response result) {
                if (result.hasSignedUp() == OptionalBoolean.FALSE) {
                    //Log.d("TEST", "requestMe-existed");
                    showSignup();
                    UserManagement.getInstance().requestSignup(new ApiResponseCallback<Long>() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            redirectLoginActivity();
                        }

                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            super.onFailure(errorResult);
                        }

                        @Override
                        public void onNotSignedUp() {
                        }

                        @Override
                        public void onSuccess(Long result) {
                            redirectLoginActivity();
                        }
                    }, null);
                } else {
                    Log.d("TEST", "requestMe-redirectMainA");
                    Logger.d("user id : " + result.getId());
                    Logger.d("email: " + result.getKakaoAccount().getEmail());
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(String.valueOf(result.getId()));
                    temp.add(result.getNickname());
                    temp.add(result.getKakaoAccount().getEmail());
                    //Logger.d("profile image: " + result.getKakaoAccount().getProfileImagePath());
                    redirectMainActivity(temp);
                }
            }
        });
    }

    private void redirectMainActivity(ArrayList<String> temp) {
        final Intent intent = new Intent(this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putStringArrayListExtra("inf", temp);
        startActivity(intent);
        finish();
    }
}

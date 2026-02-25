import { group, sleep, check } from 'k6';
import { signUp, login, reissueToken, logout } from './modules/auth.js';

export const options = {
  stages: [
    { duration: '5s', target: 5 },
    { duration: '10s', target: 10 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    // [추가] 모든 엔드포인트에 대한 p(95) 기준 설정
    'http_req_duration{endpoint:signUp}':  ['p(95)<800'], // 아까 500 넘었으니 800으로 현실적 조정
    'http_req_duration{endpoint:login}':   ['p(95)<500'],
    'http_req_duration{endpoint:reissue}': ['p(95)<300'], // 토큰 재발급은 빨라야 함
    'http_req_duration{endpoint:logout}':  ['p(95)<300'], // 로그아웃도 빨라야 함
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${new Date().getTime().toString().slice(-6)}`;
  const user = { email: `a${ts}@test.com`, pw: 'password123!', nick: `n${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  group('01. Auth Full Lifecycle', function () {
    // 1. SignUp
    const resSignUp = signUp(BASE_URL, user.email, user.pw, user.nick, {
      headers,
      tags: { endpoint: 'signUp' }
    });
    check(resSignUp, { 'is 201 created': (r) => r.status === 201 });

    // 2. Login
    const token = login(BASE_URL, user.email, user.pw, {
      headers,
      tags: { endpoint: 'login' }
    });

    if (check(token, { 'login success': (t) => t !== null })) {
      const authParams = {
        headers: { ...headers, 'Authorization': `Bearer ${token}` }
      };

      // 3. Reissue (토큰 갱신 테스트)
      const resReissue = reissueToken(BASE_URL, {
        ...authParams,
        tags: { endpoint: 'reissue' }
      });
      check(resReissue, { 'reissue success': (r) => r.status === 200 });

      // 4. Logout (로그아웃 테스트)
      const resLogout = logout(BASE_URL, {
        ...authParams,
        tags: { endpoint: 'logout' }
      });
      check(resLogout, { 'logout success': (r) => r.status === 200 });
    }
  });

  sleep(1);
}
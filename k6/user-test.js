import { group, sleep, check } from 'k6';
import { signUp, login } from './modules/auth.js';
import { getMyInfo, getUser, updateUser, searchUsers, deleteUser } from './modules/users.js';

export const options = {
  stages: [
    { duration: '5s', target: 5 },
    { duration: '10s', target: 10 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    'http_req_duration{endpoint:getMyInfo}':  ['p(95)<300'],
    'http_req_duration{endpoint:getUser}':    ['p(95)<300'],
    'http_req_duration{endpoint:updateUser}': ['p(95)<500'],
    'http_req_duration{endpoint:searchUser}': ['p(95)<800'],
    'http_req_duration{endpoint:deleteUser}': ['p(95)<400'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${new Date().getTime().toString().slice(-6)}`;
  const user = { email: `u${ts}@test.com`, pw: 'password123!', nick: `n${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  // [사전 준비] 가입 및 로그인
  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    const authParams = {
      headers: { ...headers, 'Authorization': `Bearer ${token}` }
    };
    let myId = null;

    group('02. User Domain Performance', function () {
      // 1. 내 정보 조회 (ID 획득)
      const meRes = getMyInfo(BASE_URL, { ...authParams, tags: { endpoint: 'getMyInfo' } });
      if (check(meRes, { 'getMyInfo OK': (r) => r.status === 200 })) {
        myId = meRes.json().data.id;
      }

      if (myId) {
        // 2. 특정 유저 조회
        getUser(BASE_URL, myId, { ...authParams, tags: { endpoint: 'getUser' } });

        // 3. 유저 정보 수정 (UserUpdateRequest 스펙 준수: nickname, email)
        const updatePayload = { nickname: `fixed_${user.nick}`, email: user.email };
        updateUser(BASE_URL, myId, updatePayload, { ...authParams, tags: { endpoint: 'updateUser' } });

        // 4. 유저 검색
        searchUsers(BASE_URL, { nickname: 'fixed_' }, { ...authParams, tags: { endpoint: 'searchUser' } });

        // 5. 회원 탈퇴 (테스트 데이터 정리)
        deleteUser(BASE_URL, myId, { ...authParams, tags: { endpoint: 'deleteUser' } });
      }
    });
  }

  sleep(1);
}
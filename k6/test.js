import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,      // 가상 유저 10명
  duration: '30s', // 30초 동안 실행
};

export default function () {
  // 로컬 IDE(IntelliJ 등)에서 실행 중인 백엔드 주소
  const res = http.get('http://host.docker.internal:8080/api/v1/posts');

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
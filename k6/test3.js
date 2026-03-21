// k6/squad-direct-join-once.js
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    scenarios: {
        squad_join_once: {
            executor: "per-vu-iterations",
            vus: 20,
            iterations: 1,
            maxDuration: "1m",
        },
    },
};

const loginFails = new Counter("login_fails");
const joinServerFails = new Counter("join_server_fails");

const BASE_URL = "http://spring:8080";
const PASSWORD = "1234abcd!";
const TARGET_SQUAD_ID = 1001; // DIRECT 테스트용 스쿼드

function login(email, password) {
    const res = http.post(
        `${BASE_URL}/api/auth/signin`,
        JSON.stringify({ email, password }),
        { headers: { "Content-Type": "application/json" } }
    );

    const ok = check(res, {
        "login status is 200": (r) => r.status === 200,
        "login has token": (r) => {
            try {
                const body = r.json();
                return !!(body.accessToken || body.access_token || body.token);
            } catch (_) {
                return false;
            }
        },
    });

    if (!ok) {
        loginFails.add(1);
        console.error(`LOGIN FAIL user=${email} status=${res.status} body=${res.body}`);
        return null;
    }

    const body = res.json();
    return body.accessToken || body.access_token || body.token;
}

export default function () {
    const email = `user${__VU}@test.com`;
    const token = login(email, PASSWORD);
    if (!token) return;

    const res = http.post(
        `${BASE_URL}/api/squad/${TARGET_SQUAD_ID}/join`,
        null,
        {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        }
    );

    // 서버 에러만 실패로 보고, 비즈니스 실패(400 등)는 따로 body로 해석
    const ok = check(res, {
        "join not 5xx": (r) => r.status < 500,
    });

    if (!ok) {
        joinServerFails.add(1);
        console.error(`JOIN FAIL user=${email} status=${res.status} body=${res.body}`);
    } else {
        console.log(`JOIN RESULT user=${email} status=${res.status} body=${res.body}`);
    }

    sleep(0.1);
}
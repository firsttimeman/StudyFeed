import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    scenarios: {
        hot_feed_like_stress: {
            executor: "constant-vus",
            vus: 20,              // 현재 더미 유저 수에 맞춤
            duration: "5m",       // 5분 동안 계속 요청
            gracefulStop: "10s",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<1000"],
    },
};

const loginFails = new Counter("login_fails");
const likeFails = new Counter("like_fails");

const BASE_URL = "http://spring:8080";
const HOT_FEED_ID = 1;
const PASSWORD = "1234abcd!";

// VU별로 1번만 로그인하고 토큰 재사용
let cachedToken = null;

function login(email, password) {
    const payload = JSON.stringify({
        email,
        password,
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const res = http.post(`${BASE_URL}/api/auth/signin`, payload, params);

    const ok = check(res, {
        "login status is 200": (r) => r.status === 200,
        "login has access token": (r) => {
            try {
                const body = r.json();
                return !!(body.accessToken || body.access_token || body.token);
            } catch (e) {
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

function getToken() {
    if (cachedToken) {
        return cachedToken;
    }

    const email = `user${__VU}@test.com`;
    cachedToken = login(email, PASSWORD);
    return cachedToken;
}

export default function () {
    const token = getToken();
    if (!token) {
        return;
    }

    const params = {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };

    const res = http.post(`${BASE_URL}/api/feed/${HOT_FEED_ID}/like`, null, params);

    const ok = check(res, {
        "like status is 200": (r) => r.status === 200,
    });

    if (!ok) {
        likeFails.add(1);
        console.error(`LIKE FAIL user=user${__VU}@test.com status=${res.status} body=${res.body}`);
    }

    // 완전 무지성 폭격 말고 아주 짧게만 쉼
    sleep(0.1);
}
import axios from "axios";

const API_BASE =
    import.meta.env.VITE_API_BASE?.trim() || "http://localhost:5000";

const api = axios.create({
    baseURL: API_BASE,
    timeout: 8000,
});

export async function fetchResults(params = {}) {
    const res = await api.get("/api/results", { params });
    return res.data;
}

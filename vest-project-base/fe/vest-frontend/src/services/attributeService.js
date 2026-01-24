import http from "./http";

const getEndpoint = (type) => `/api/${type}`;

export default {
    getAll(type, params) {
        return http.get(getEndpoint(type), { params });
    },
    getAllList(type) {
        return http.get(`${getEndpoint(type)}/list`);
    },
    getById(type, id) {
        return http.get(`${getEndpoint(type)}/${id}`);
    },
    create(type, data) {
        return http.post(getEndpoint(type), data);
    },
    update(type, id, data) {
        return http.put(`${getEndpoint(type)}/${id}`, data);
    },
    delete(type, id) {
        return http.delete(`${getEndpoint(type)}/${id}`);
    }
};

import { http } from "../utils/request";
export function recordApi(admin = false) {
  const path = admin ? "/admin/records" : "/records";
  return {
    list: (params) => http.get(path, { params }),
    detail: (id) => http.get(path + "/" + id),
    create: (data) => http.post(path, data),
    update: (id, data) => http.put(path + "/" + id, data),
    remove: (id) => http.delete(path + "/" + id),
  };
}

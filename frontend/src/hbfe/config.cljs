(ns hbfe.config)

(def api-url "http://localhost:8000/")

(def login-url (str api-url "login"))
(def profile-url (str api-url "profile"))
(def repositories-url (str api-url "repositories"))
(defn repository-detail-url [repo_id]
  (str repositories-url "/" repo_id))
(defn repository-jobs-url [repo_id]
  (str repositories-url "/" repo_id "/jobs"))
(defn repository-jobs-detail-url [repo_id job_id]
  (str repositories-url "/" repo_id "/jobs/" job_id))

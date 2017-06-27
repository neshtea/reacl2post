(ns reacl2post.core
  (:require [active.clojure.record :as r :include-macros true]
            [active.clojure.lens :as lens]
            [reacl2.core :as reacl2 :include-macros true]
            [reacl2.dom :as dom]))

;; Ein Record, um Anfragen (`request`) und den Empfänger der
;; Antwort (`recipient`) darzustellen.
(r/define-record-type message
  (make-message recipient request) message?
  [recipient message-recipient  ;; Referenz zur Empfängerkomponente.
   request message-request      ;; Anfrage, die an den Server gestellt werden soll.
   ])

(defn handle-action
  [state req]
  (cond
    (message? req)
    (let [;; Suche daten von einem Server.
          ;; Für dieses Beispiel geben wir einfach einen Vektor
          ;; von Strings zurück.
          response-data ["foo" "bar" "baz"]]
      ;; Wir bauen eine künstliche Verzögerung ein, um den Aufbau der Seite
      ;; auch im Browser betrachten zu können.
      (js/setTimeout
       #(reacl2/send-message! (message-recipient req) response-data)
       2000))))

;; Ein Post besteht aus:
;; * einer `id` (integer)
;; * einem `body` (string)
(r/define-record-type post
  (make-post id body) post?
  [id post-id
   body post-body])

;; Einige Bespielposts.
(def post-1 (make-post 0 "Reacl 2 is out! Time to celebrate!"))
(def post-2 (make-post 1 "Go read the post on Funktionale Programmierung"))

;; Ein Vektor von initialen Posts.
(def initial-posts [post-1 post-2])

;; Eine Reacl-Klasse zur Darstellung eines Posts.
(reacl2/defclass post-detail this post [parent]  ;; 1. App state and arguments.
  local-state [comments []]  ;; 2. Local state.

  component-did-mount
  #(reacl2/return
    :action
    (make-message this [:fetch-comments-for-post (post-id post)]))

  ;; 3. Tell reacl how to render this component.
  render
  (dom/div

   ;; We can access the app-state (aka. `post`) here.
   (dom/h1 "Post " (post-id post)) 
   (dom/p (post-body post))

   ;; 4. Button to get back to the post list.
   (dom/button {:onclick #(reacl2/send-message! parent [:back])} "back")

   ;; 5. Render a list of comments.
   (dom/div
    (dom/h2 "Comments")
    (if (empty? comments)
      (dom/p "Loading comments...")
      (dom/ul
       (map-indexed (fn [idx comment]
                      (dom/keyed (str "comment-" idx)
                                 (dom/li comment))) comments)))))

  handle-message
  (fn [comments]
    (reacl2/return :local-state comments)))

;; Der Zustand der `post-component` besteht aus
;; * einer liste von `posts`
;; * einem optionalen `post`, welcher im Detail angezeigt werden soll.
(r/define-record-type post-component-state
  (make-post-component-state posts post-detail) post-component-state?
  [posts post-component-state-posts
   post-detail post-component-state-post-detail])

;; Eine Komponente zur Darstellung einer Liste von Posts.
(reacl2/defclass post-component this []
  local-state [state (make-post-component-state initial-posts nil)]

  render
  (if-let [post (post-component-state-post-detail state)]
    ;; Ein Post soll detailliert angezeigt werden.
    (post-detail post this)
    ;; Kein Detail, zeige ganze Liste.
    (dom/ul
     (map (fn [post]
            (dom/keyed (str (post-id post))
                       (dom/li
                        (dom/a
                         {:onclick #(reacl2/send-message! this [:detail post])
                          :href "#"}
                         (post-body post)))))
          (post-component-state-posts state))))

  handle-message
  (fn [msg]
    (case (first msg)
      :back
      (reacl2/return :local-state (assoc state :post-detail nil))
      :detail
      (reacl2/return :local-state (assoc state :post-detail (second msg))))))

(reacl2/render-component (.getElementById js/document "app")
                         post-component
                         (reacl2/opt :reduce-action handle-action))

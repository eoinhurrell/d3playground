(ns d3playground.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [d3playground.bar :refer [barh-plot]]
            [cljsjs.d3]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Model

(defonce app-state
  (reagent/atom
   {:width 500
    :height 800
    :margin {:top 10 :left 50 :bottom 20 :right 10}
    :data  [{:x 5 :label "John"}
            {:x 2 :label "Paul"}
            {:x 3 :label "George"}
            {:x 1 :label "Ringo"}]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Util Fns
(defn randomize-data [ratom]
  (let [points-n (max 2 (rand-int 50))
        points   (range points-n)
        create-x (fn [] (max 1 (rand-int 501)))
        create-label (fn [] (rand-nth ["John" "Paul" "George" "Ringo"]))]
    (swap! ratom update :data
           (fn []
             (mapv #(hash-map :x (create-x)
                              :label (create-label))
                   points)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Components

(defn btn-toggle-width [ratom]
  [:div
   [:button
    {:on-click #(swap! ratom update
                       :width (fn [width]
                                (if (= 300 width) 500 300)))}
    "Toggle width"]])


(defn btn-randomize-data [ratom]
  [:div
   [:button
    {:on-click #(randomize-data ratom)}
    "Randomize data"]])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Page

(defn app []
  [:div
   [:div {:class "col-md-1"}]
   [:div {:class "col-md-10"}
    [:h1 "Barcharts"]
    [barh-plot app-state]
    ;; [btn-randomize-data app-state]
    ]
   [:div {:class "col-md-1"}]
   ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(let []
  ;; (dispatch-sync [:initialize-db])
  (reagent/render-component [app]
                            (. js/document (getElementById "app"))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

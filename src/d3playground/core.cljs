(ns d3playground.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                                path
                                                register-sub
                                                dispatch
                                                dispatch-sync
                                                subscribe]]
            [cljsjs.d3]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Model

(defonce app-state
  (reagent/atom
   {:width 300
    :data  [{:x 5}
            {:x 2}
            {:x 3}]}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Util Fns

(defn get-width [ratom]
  (:width @ratom))

(defn get-height [ratom]
  (let [width (get-width ratom)]
    (* 0.8 width)))

(defn get-data [ratom]
  (:data @ratom))


(defn randomize-data [ratom]
  (let [points-n (max 2 (rand-int 50))
        points   (range points-n)
        create-x (fn [] (max 1 (rand-int 5)))]
    (swap! ratom update :data
           (fn []
             (mapv #(hash-map :x (create-x))
                   points)))))

(defn append-data [ratom]
    (swap! ratom update-in [:data] conj {:x (max 1 (rand-int 5))}))

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


(defn btn-append-data [ratom]
  [:div
   [:button
    {:on-click #(append-data ratom)}
    "Append data"]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Viz

;; Container

(defn container-did-mount [ratom]
  (-> (js/d3.select "#barchart svg")
      (.append "g")
      (.attr "class" "container")
      (.append "g")
      (.attr "transform"  (str "translate(0," 350 ")"))
      (.call (-> js/d3 (.axisBottom (-> js/d3
                                        .scaleLinear
                                        (.domain #js [0 5])
                                        (.range #js [0 400])))))
      ))

;; Bars

(defn bars-enter [ratom]
  (let [data (get-data ratom)]
    (-> (js/d3.select "#barchart svg .container .bars")
        (.selectAll "rect")
        (.data (clj->js data))
        .enter
        (.append "rect"))))

(defn bars-update [ratom]
  (let [width   (get-width ratom)
        height  (get-height ratom)
        data    (get-data ratom)
        data-n  (count data)
        rect-height (/ height data-n)
        x-scale (-> js/d3
                    .scaleLinear
                    (.domain #js [0 5])
                    (.range #js [0 width]))]
    (-> (js/d3.select "#barchart svg .container .bars")
        (.selectAll "rect")
        (.data (clj->js data))
        (.attr "fill" "green")
        (.transition)
        (.duration 300)
        (.attr "x" (x-scale 0))
        (.transition)
        (.duration 300)
        (.attr "y" (fn [_ i]
                     (* i rect-height)))
        (.attr "height" (- rect-height 1))
        (.attr "width" (fn [d]
                         (x-scale (aget d "x"))))
        )
    ))

(defn bars-exit [ratom]
  (let [width   (get-width ratom)
        data (get-data ratom)
        x-scale (-> js/d3
                    .scaleLinear
                    (.domain #js [0 5])
                    (.range #js [0 width]))]
    (-> (js/d3.select "#barchart svg .container .bars")
        (.selectAll "rect")
        (.data (clj->js data))
        .exit
        (.transition)
        (.duration 300)
        (.attr "x" (x-scale 0))
        (.attr "width" width - (x-scale 0))
        (.style "fill-opacity" 1e-6)
        .remove)))

(defn bars-did-update [ratom]
  (bars-enter ratom)
  (bars-update ratom)
  (bars-exit ratom))

(defn bars-did-mount [ratom]
  (-> (js/d3.select "#barchart svg .container")
      (.append "g")
      (.attr "class" "bars"))
  (bars-did-update ratom))


;; Main

(defn barh-plot-render [ratom]
  (let [width  (get-width ratom)
        height (get-height ratom)]
    [:div
     {:id "barchart"}

     [:svg
      {:width  width
       :height height}]]))

(defn barh-plot-did-mount [ratom]
  (container-did-mount ratom)
  (bars-did-mount ratom))

(defn barh-plot-did-update [ratom]
  (bars-did-update ratom))

(defn barh-plot [ratom]
  (reagent/create-class
   {:reagent-render      #(barh-plot-render ratom)
    :component-did-mount #(barh-plot-did-mount ratom)
    :component-did-update #(barh-plot-did-update ratom)}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Page

(defn app []
  [:div
   [:div {:class "col-md-1"}]
   [:div {:class "col-md-10"}
    [:h1 "Barchart"]
    [barh-plot app-state]
    [btn-toggle-width app-state]
    [btn-randomize-data app-state]
    [btn-append-data app-state]
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

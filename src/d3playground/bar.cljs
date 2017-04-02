(ns d3playground.bar
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
  ))

(defn get-width [ratom]
  (:width @ratom))

(defn get-height [ratom]
  (:height @ratom))

(defn get-data [ratom]
  (:data @ratom))

(defn get-margin [ratom side]
  (side (:margin @ratom)))

(defn get-plot-width [ratom]
  (- (get-width ratom)
     (+ (get-margin ratom :left)
        (get-margin ratom :right))))

(defn get-plot-height [ratom]
  (- (get-height ratom)
     (+ (get-margin ratom :top)
        (get-margin ratom :bottom))))

(defn get-x-scale [ratom]
  (let [width (get-plot-width ratom)]
    (-> js/d3
        .scaleLinear
        (.domain #js [0 500])
        (.range #js [0 width]))))

(defn get-y-scale [ratom]
  (.. js/d3
      (scaleBand)
      (range #js [(get-plot-height ratom) 0])
      (padding 0.1)
      (domain #js ["John" "Paul" "George" "Ringo"])
      ))

(defn container-did-mount [ratom]
  (-> (js/d3.select "#horizontal-barchart svg")
      (.attr "width" (+ (get-width ratom)
                        (get-margin ratom :left)
                        (get-margin ratom :right)))
      (.attr "height" (+ (get-height ratom)
                         (get-margin ratom :top)
                         (get-margin ratom :bottom)))
      (.append "g")
      (.attr "class" "container")
      (.append "g")
      (.attr "transform"  (str
                           "translate("
                           (get-margin ratom :left)
                           ",0)"))
      (.call (-> js/d3 (.axisLeft (get-y-scale ratom))))
      (.append "g")
      (.attr "transform"  (str
                           "translate(0,"
                           (- (get-height ratom)
                              (+ (get-margin ratom :bottom)
                                 (get-margin ratom :top)))
                           ")"))
      (.call (-> js/d3 (.axisBottom (get-x-scale ratom))))
      ))

;; Bars

(defn bars-enter [ratom]
  (let [data (get-data ratom)]
    (-> (js/d3.select "#horizontal-barchart svg .container .bars")
        (.selectAll "rect")
        (.data (clj->js data))
        .enter
        (.append "rect"))))

(defn bars-update [ratom]
  (let [width   (get-plot-width ratom)
        height  (get-plot-height ratom)
        data    (get-data ratom)
        y-scale (get-y-scale ratom)
        x-scale (get-x-scale ratom)]
    (-> (js/d3.select "#horizontal-barchart svg .container .bars")
        (.attr "transform"  (str
                             "translate("
                             (get-margin ratom :left)
                             ",0)"))
        (.selectAll "rect")
        (.data (clj->js data))
        (.attr "fill" "green")
        (.transition)
        (.duration 300)
        (.attr "x" (x-scale 0))
        (.attr "width" (fn [d]
                         (x-scale (aget d "x"))))
        (.transition)
        (.duration 300)
        (.attr "y" (fn [d]
                     (y-scale (aget d "label"))))
        (.attr "height" (.bandwidth y-scale))
        )
    ))

(defn bars-exit [ratom]
  (let [width   (get-plot-width ratom)
        data (get-data ratom)
        x-scale (get-x-scale ratom)]
    (-> (js/d3.select "#horizontal-barchart svg .container .bars")
        (.selectAll "rect")
        (.data (clj->js data))
        .exit
        (.transition)
        (.duration 300)
        (.attr "x" (x-scale 0))
        (.attr "width" (- width (x-scale 0)))
        (.style "fill-opacity" 1e-6)
        .remove)))

(defn bars-did-update [ratom]
  (bars-enter ratom)
  (bars-update ratom)
  (bars-exit ratom))

(defn bars-did-mount [ratom]
  (-> (js/d3.select "#horizontal-barchart svg .container")
      (.append "g")
      (.attr "class" "bars"))
  (bars-did-update ratom))


;; Main

(defn barh-plot-render [ratom]
  (let [width  (get-width ratom)
        height (get-height ratom)]
    [:div
     {:id "horizontal-barchart"}
     [:svg]]))

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

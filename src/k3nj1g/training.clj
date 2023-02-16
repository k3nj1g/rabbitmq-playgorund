(ns k3nj1g.training
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(def ^{:const true} qname "dev-queue")

(defn send-message [& args]
  (let [conn (rmq/connect)
        ch   (lch/open conn)]
    (lq/declare ch qname {:durable false :exclusive false :auto-delete false})
    (doseq [n (range 20)]
      (Thread/sleep 1000)
      (lb/publish ch "" qname (format "Hello! %s" n)))
    (prn "Message is sent to Default Exchange")))

(defn receive-message [& args]
  (letfn ([message-handler 
           [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
           (println (format "Received message: %s" (String. payload "UTF-8")))])
    (let [conn (rmq/connect)
         ch   (lch/open conn)]
     (lq/declare ch qname {:durable false :exclusive false :auto-delete false})
     (lc/subscribe ch qname message-handler {:auto-ack true})
     (prn (format "Subscribed to the queue: %s" qname)))))



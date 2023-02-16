(ns k3nj1g.producer
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(def ^{:const true} direct-exchange "direct_logs")
(def ^{:const true} error-queue "error")

(defn send-message [sleep-time routing-key]
  (let [conn (rmq/connect)
        ch   (lch/open conn)]
    (le/declare ch direct-exchange "direct")
    (doseq [n (range 100)]
      (Thread/sleep sleep-time)
      (lb/publish ch direct-exchange routing-key (format "Message type [%s] from publisher N %s" routing-key n))
      (prn (format "Message type [%s] is sent into Direct Exchange [N: %s]" routing-key n)))))

(defn send-messages [& _]
  (let [producer-1 (future (send-message 2000 "error"))
        producer-2 (future (send-message 1500 "info"))
        producer-3 (future (send-message 1000 "warning"))]
    @producer-1))

(defn start-consumer [{:keys [routing-keys]}]
  (letfn ([message-handler
           [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
           (println (format "Received message: %s" (String. payload "UTF-8")))])
    (let [conn       (rmq/connect)
          ch         (lch/open conn)
          queue-name (:queue (lq/declare ch))]
      (le/declare ch direct-exchange "direct")
      (doseq [routing-key routing-keys]
        (lq/bind ch queue-name direct-exchange {:routing-key routing-key}))
      (lc/subscribe ch queue-name message-handler {:auto-ack true})
      (prn (format "Subscribed to the queue: %s" queue-name)))))


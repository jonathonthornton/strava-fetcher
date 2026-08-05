-- Rides and distances grouped by bike since date.
SELECT bike.name,
       COUNT(ride_activity.id)                        AS rides,
       ROUND(SUM(ride_activity.distance)::numeric, 0) AS distance
FROM strava.ride_activity
         JOIN
     strava.bike ON strava.ride_activity.gear_id = strava.bike.id
WHERE ride_activity.start_date_local > '2025-01-01'
GROUP BY bike.name
ORDER BY rides DESC;
-- Long rides grouped by bike.
SELECT
    bike.name,
    COUNT(ride_activity.id) AS rides
FROM
    strava.ride_activity
        JOIN
    strava.bike ON strava.ride_activity.gear_id = strava.bike.id
WHERE
    ride_activity.distance >= 200
GROUP BY
    bike.name
ORDER BY
    rides DESC;
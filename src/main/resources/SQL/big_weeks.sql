SELECT
    TO_CHAR(DATE_TRUNC('week', start_date_local), 'YYYY-MM-DD') AS week_start_date,
    STRING_AGG(ride_activity.name, ', ') AS ride_names,
    COUNT(*) AS ride_count
FROM
    strava.ride_activity
WHERE
    sport_type IN ('Ride', 'MountainBikeRide', 'GravelRide')
    AND round(ride_activity.distance::numeric, 1) > 0
GROUP BY
    DATE_TRUNC('week', start_date_local)
HAVING
    COUNT(*) >= 6
ORDER BY
    week_start_date DESC;
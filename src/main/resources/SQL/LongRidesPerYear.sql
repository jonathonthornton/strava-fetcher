-- Long rides per year.
SELECT
    EXTRACT(YEAR FROM start_date_local) AS year,
    COUNT(*) AS number_of_rides
FROM
    strava.ride_activity
WHERE
    distance >= 200
GROUP BY
    year
ORDER BY
    number_of_rides DESC;
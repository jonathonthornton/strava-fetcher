SELECT f.first_name, f.last_name
FROM strava.follower f
WHERE EXISTS (
    -- Check for kudos given between 365 days and 90 days ago
    SELECT 1
    FROM strava.kudos k
             JOIN strava.ride_activity r ON r.id = k.activity_id
    WHERE k.follower_id = f.id
      AND r.start_date_local BETWEEN CURRENT_DATE - INTERVAL '365 days' AND CURRENT_DATE - INTERVAL '90 days'
)
  AND NOT EXISTS (
    -- Ensure no kudos were given in the last 90 days
    SELECT 1
    FROM strava.kudos k
             JOIN strava.ride_activity r ON r.id = k.activity_id
    WHERE k.follower_id = f.id
      AND r.start_date_local > CURRENT_DATE - INTERVAL '90 days'
)
ORDER BY f.first_name

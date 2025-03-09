CREATE UNIQUE INDEX UK_reservation_room_id_start_time_end_time
    ON reservation (room_id, start_time, end_time);

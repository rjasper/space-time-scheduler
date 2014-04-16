function Os = obstacles_fixture

Os = {
    regular_polygon(6, [4  4], 1)
    regular_polygon(3, [5 11], 3)
    regular_polygon(4, [9  6], 2)
    regular_polygon(4, [11 11], sqrt(2), pi/4)
    regular_polygon(3, [14 7], 2, deg2rad(180))
}';
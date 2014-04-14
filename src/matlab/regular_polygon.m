function [X, Y] = regular_polygon(n, x, y, a, alpha)

phi_offset = pi/2 - pi/n; % align base
delta_phi = 2*pi/n;
phi = linspace(0, 2*pi - delta_phi, n) - phi_offset + alpha;

r = a * cos(delta_phi/2)/sin(delta_phi);

X = r*cos(phi) + x;
Y = r*sin(phi) + y;
function [X, Y] = regular_polygon(n, z, a, alpha)

x = z(1);
y = z(2);

if nargin == 3
    alpha = 0;
end

phi_offset = pi/2 - pi/n; % align base
dphi = 2*pi/n;
phi = linspace(0, 2*pi - dphi, n) - phi_offset + alpha;

r = a * cos(dphi/2)/sin(dphi);

X = r*cos(phi) + x;
Y = r*sin(phi) + y;

if nargout == 1
    X = [X; Y];
end
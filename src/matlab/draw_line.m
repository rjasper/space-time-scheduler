function draw_line(l, varargin)
%DRAW_LINES   plots a set of line segments
%
% arguments:
%   segs: the line segments to be plotted

%% convenience variables

x1 = l(1, :);
y1 = l(2, :);
x2 = l(3, :);
y2 = l(4, :);

%% processing

delta_x = x2 - x1;
delta_y = y2 - y1;

%% plot

quiver(x1, y1, delta_x, delta_y, 0, varargin{:});

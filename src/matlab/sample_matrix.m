function samples = sample_matrix(mat, pts)
%SAMPLE_MATRIX Retrieves sample points of the given matrix.
% 
% arguments:
%   mat: Matrix to be sampled.
%        Must have at least as many dimension as size(pts)(end).
%   pts: The coordinates of the matrix to be sampled.
%        size(pts)(1:end-1) will be the grid of the resulting sample.
%        The last dimension holds the sampling coordinates.
% returns:
%   samples: A sampled version of the matrix.
%        If the dimension of the matrix is higher than size(pts)(end) the
%        remaining dimensions will be retained.
%
% example:
%   % a rgb image to be sampled
%   img = imread('fabric.png'); % <480x640x3 uint8>
%   % a 2x2 grid of 2 dimensional sample coordinates
%   pts(1, 1, :) = [10 10];
%   pts(1, 2, :) = [10 20];
%   pts(2, 1, :) = [20 10];
%   pts(2, 2, :) = [35 35];
%
%   % this will result in a 2x2x3 sampled version of the rgb image
%   % note that the 3 color channels are retained as the sample coordinates
%   % only used two of the three available matrix dimensions
%   sampled = sample_matrix(img, pts);
% 

%% convenience variables

size_pts = size(pts);
size_mat = size(mat);

dim_mat = ndims(mat);

% the size of the grid to be filled with samples
grid_size = size_pts(1:end-1);
% dimension number of the sample coordinates
coord_dim = size_pts(end);
% the space of valid coordinates
coord_size = size_mat(1:coord_dim);
% the size of a value at a coordinate
value_size = size_mat(coord_dim+1:end);
% the size of the sample
sample_size = [grid_size value_size];

% make sure that the size is at least two dimensional
if length(sample_size) == 1
    sample_size = [1 sample_size];
end

%% check

if dim_mat < coord_dim
    error('coordinate dimension too high');
end

%% processing

% reshapes the points into two dimensions:
% - the first dimension represents the linearized coordinates (grid) of the sample
% - the last dimension represents coordinates of the sampling points of the
%   matrix
pts_lin = reshape(pts, [], coord_dim);

% linearizes the sample coordinates indices
dim_sub = mat2cell(pts_lin, size(pts_lin, 1), ones(1, coord_dim));
idx = sub2ind(coord_size, dim_sub{:});
% idx = (pts_lin - 1) * [1 cumprod(coord_size(1:end-1))]' + 1;

% if the matrix has more dimension than the points to be sampled
if dim_mat == coord_dim
    % trivial sampling step
    samples = mat(idx);
else % dim_mat > size_pts(end)
    % reshape the matrix into two dimensions
    % - the first dimension covers the linearized coordinates to be sampled
    % - the last dimension covers the linearized values to be sampled
    mat_lin = reshape(mat, prod(coord_size), prod(value_size));
    
    % the actual sampling step
    samples = mat_lin(idx, :);
end

% reshapes the sample with the grid given by the first dimensions of the
% sample coordinates and the remaining dimensions of the matrix.
samples = reshape(samples, sample_size);

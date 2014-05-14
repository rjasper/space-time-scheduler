function draw3D_polygon(P, path, varargin)

if isempty(P)
    return;
end

cellfun(@helper, P, path)

    function helper(P, path)
        n_P = size(P, 2);
        n_path = size(path, 2);
        
        V = repmat([P; zeros(1, n_P)], 1, n_path) + reshape(repmat(path, n_P, 1), 3, []);
        
        F1 = reshape(1:n_path*n_P, n_P, n_path)';
        F2 = [
            reshape(F1(1:end-1,  1:end   ), [], 1) ...
            reshape(F1(2:end  ,  1:end   ), [], 1) ...
            reshape(F1(2:end  , [2:end 1]), [], 1) ...
            reshape(F1(1:end-1, [2:end 1]), [], 1) ...
        ];
        
        patch('Vertices', V', 'Faces', F1([1 end], :), varargin{:});
        patch('Vertices', V', 'Faces', F2            , varargin{:});
    end

end
function calc_st_space(Om, path)

N_Om = size(Om, 2);
N_path = size(path, 2);

v = cellfun(@calc_velocity, {Om{:}.path}, 'UniformOutput', false);
l = calc_path_length(path);
s_i = cumsum([0 l]);

Om_st = cellfun(@calc_Om_st, ...
    {Om{:}.polygon}, ...
    {Om{:}.path}, ...
    v, ...
    'UniformOutput', false);

    function v = calc_velocity(path)
        dpath = diff(path, 1, 2);
        
        % v := [x; y] / t
        v = dpath(1:2, :) ./ repmat(dpath(3, :), 2, 1);
    end

    function Om_st = calc_Om_st(polygon, path_Om, v)
%         n_path_Om = size(path_Om, 2);
        n_v = size(v, 2);
        
        p1 = path_Om(:, 1:end-1);
        p2 = path_Om(:, 2:end);
        
        Om_st = cellfun(@calc_Om_st_v, ...
            mat2cell( p1, 3, ones(1, n_v) ), ...
            mat2cell( p2, 3, ones(1, n_v) ), ...
            mat2cell( v , 2, ones(1, n_v) ));
        
        function Om_st_j = calc_Om_st_v(p1, p2, v_j)
            t1 = p1(3);
            t2 = p2(3);
            
            Om_st_j = {};
        end
    end


end
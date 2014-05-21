function XYT = calc_xyt_path(V_xy, path_xy, V_st, path_st)

XY = V_xy(:, path_xy);
ST = V_st(:, path_st);

l_XY = path_length(V_xy, path_xy);
s_XY = [0 cumsum(l_XY)];

t_XY = arrayfun(@calc_t, s_XY);
[x_ST, y_ST] = arrayfun(@calc_xy, ST(1, :));

XYT_ = [[XY; t_XY] [x_ST; y_ST; ST(2, :)]];
[t_, t_idx_] = sort([t_XY ST(2, :)]);
t_idx = t_idx_( [true, diff(t_) ~= 0] );
XYT = XYT_(:, t_idx); % TODO: use epsilon

    function t = calc_t(s)
        % TODO: use epsilon
        
        k = find(ST(1, :) >= s, 1, 'first');
        
        if s == ST(1, k)
            t = ST(2, k);
        else
            ST1 = ST(:, k-1);
            ST2 = ST(:, k);
            dST = ST2 - ST1;

            % a = (s - s1) / (s2 - s1)
            a = (s - ST1(1)) / dST(1);
            t = a*dST(2) + ST1(2);
        end
    end

    function [x, y] = calc_xy(s)
        % TODO: use epsilon
        
        k = find(s_XY >= s, 1, 'first');
        
        if s == s_XY(k)
            xy = XY(:, k);
        else
            s1 = s_XY(k-1);
            s2 = s_XY(k);
            ds = s2 - s1;
            
            XY1 = XY(:, k-1);
            XY2 = XY(:, k);
            dXY = XY2 - XY1;
            
            a = (s - s1) / ds;
            xy = a*dXY + XY1;
        end
        
        x = xy(1, :);
        y = xy(2, :);
    end

end
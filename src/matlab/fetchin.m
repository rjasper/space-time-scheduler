function val = fetchin(ws, var)

ws_vars = evalin(ws, 'who');

if ismember(var, ws_vars)
    val = evalin(ws, var);
else
    error('unknown variable ''%s''', var);
end

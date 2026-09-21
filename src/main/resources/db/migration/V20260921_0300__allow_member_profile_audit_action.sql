ALTER TABLE public.admin_action_logs
    DROP CONSTRAINT IF EXISTS admin_action_logs_action_check;

ALTER TABLE public.admin_action_logs
    ADD CONSTRAINT admin_action_logs_action_check CHECK (action IN (
        'HIDE_POST', 'RESTORE_POST', 'DELETE_POST', 'LOCK_POST', 'UNLOCK_POST',
        'HIDE_COMMENT', 'RESTORE_COMMENT', 'DELETE_COMMENT',
        'SANCTION_USER', 'HANDLE_REPORT', 'UPDATE_USER_ROLE', 'UPDATE_USER_PROFILE'
    ));

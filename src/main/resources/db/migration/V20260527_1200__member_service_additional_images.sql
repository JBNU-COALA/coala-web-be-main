CREATE TABLE IF NOT EXISTS member_service_additional_images (
    service_id varchar(80) NOT NULL REFERENCES member_services(service_id) ON DELETE CASCADE,
    display_order integer NOT NULL,
    image_url varchar(500) NOT NULL,
    PRIMARY KEY (service_id, display_order)
);

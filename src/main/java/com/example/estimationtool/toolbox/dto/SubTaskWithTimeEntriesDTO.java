package com.example.estimationtool.toolbox.dto;

import com.example.estimationtool.model.SubTask;
import com.example.estimationtool.model.TimeEntry;

import java.util.List;

public record SubTaskWithTimeEntriesDTO(SubTask subTask, List<TimeEntry> timeEntries) {
}

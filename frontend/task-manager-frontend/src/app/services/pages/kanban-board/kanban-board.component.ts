import {Component} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-kanban-board',
  standalone: true,
  imports: [
    CdkDropList,
    CdkDrag,
    NgForOf
  ],
  templateUrl: './kanban-board.component.html',
  styleUrl: './kanban-board.component.scss'
})
export class KanbanBoardComponent {

  columns = [
    {
      title: 'To Do',
      tasks: ['Task 1', 'Task 2', 'Task 3']
    },
    {
      title: 'In Progress',
      tasks: ['Task 4', 'Task 5']
    },
    {
      title: 'Done',
      tasks: ['Task 6']
    }
  ];

  drop(event: CdkDragDrop<any[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    }
  }

}
